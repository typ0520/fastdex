package bizsocket.core;

import bizsocket.core.internal.RequestContextQueue;
import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import bizsocket.tcp.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by tong on 16/3/7.
 */
public class RequestQueue implements PacketListener,ConnectionListener {
    protected final Logger logger = LoggerFactory.getLogger(SocketConnection.class.getSimpleName());
    private final List<RequestContext> requestContextList = new RequestContextQueue();
    private final Set<SerialSignal> serialSignalList = Collections.synchronizedSet(new HashSet<SerialSignal>());
    private final List<AbstractSerialContext> mSerialContexts = Collections.synchronizedList(new ArrayList<AbstractSerialContext>());
    private final InterceptorChain interceptorChain;
    private final AbstractBizSocket bizSocket;
    private ResponseHandler globalNotifyHandler;

    public RequestQueue(AbstractBizSocket bizSocket) {
        this.bizSocket = bizSocket;
        interceptorChain = new InterceptorChain();

        bizSocket.getSocketConnection().addPacketListener(this);
        bizSocket.getSocketConnection().addConnectionListener(this);
    }

    public void addRequestContext(final RequestContext context) {
        if (context == null) {
            return;
        }
        prepareContext(context);
        boolean existed = requestContextList.contains(context);
        if (!existed) {
            if ((context.getFlags() & RequestContext.FLAG_CLEAR_QUEUE) != 0) {
                removeRequestContexts(requestContextList);
            }
            if ((context.getFlags() & RequestContext.FLAG_NOT_SUPPORT_REPEAT) != 0) {
                Collection contexts = getRequestContext(new Filter() {
                    @Override
                    public boolean filter(RequestContext ctx) {
                        return ctx.getRequestCommand() == context.getRequestCommand();
                    }
                });

                if (!contexts.isEmpty()) {
                    //TODO 如果发现重复需要选择一种策略,移除之前的把当前的添加进去，或者当前的不加入队列
                    return;
                }
            }
            if ((context.getFlags() & RequestContext.FLAG_JUMP_QUEUE) != 0) {
                requestContextList.add(0,context);
            }
            else {
                requestContextList.add(context);
            }
            InterceptorChain chain = getInterceptorChain();
            boolean result = chain.invokePostRequestHandle(context);
            if (result) {
                RequestInterceptedException exception = new RequestInterceptedException("请求被拦截");
                context.sendFailureMessage(context.getRequestCommand(), exception);
                removeRequestContext(context);
            }
            else {
                dealSerialSignal(context);
                sendRequest(context);
            }
        }
    }

    private void dealSerialSignal(RequestContext context) {
        SerialSignal serialSignal = getSerialSignal(context.getRequestCommand());
        //判断是否是串行入口命令
        if (serialSignal != null) {
            AbstractSerialContext serialContext = getSerialContext(context);
            if (serialContext == null) {
                try {
                    serialContext = buildSerialContext(serialSignal,context);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                logger.debug("build serial context: " + serialContext);
                mSerialContexts.add(serialContext);
            } else {
                logger.debug("repeat request: " + serialContext);
            }
            return;
        }

        removeExpiredSerialContexts();
    }

    /**
     * 加入队列前准备上下文
     * @param requestContext
     */
    private void prepareContext(final RequestContext requestContext) {
        requestContext.setOnRequestTimeoutListener(new RequestContext.OnRequestTimeoutListener() {
            @Override
            public void onRequestTimeout(RequestContext context) {
                //请求超时
                RequestTimeoutException exception = new RequestTimeoutException("请求超时，请检查网络连接");
                context.sendFailureMessage(context.getRequestCommand(), exception);
                logger.debug("request timeout: " + requestContext);
                removeRequestContext(context);
            }
        });
    }

    protected void recyclePacket(Packet packet) {
        if (packet != null) {
            packet.recycle();
        }
    }

    public void removeRequestContext(final RequestContext context) {
        removeRequestContexts(new ArrayList<RequestContext>(){{add(context);}});
    }

    public void removeRequestContexts(Collection<RequestContext> requestContexts) {
        if (requestContexts == null) {
            return;
        }
        requestContextList.removeAll(requestContexts);

        for (RequestContext context : requestContexts) {
            recyclePacket(context.getRequestPacket());
        }
    }

    public Collection<RequestContext> getRequestContext(Filter filter) {
        if (filter == null) {
            throw new RuntimeException("filter can not be null");
        }
        List<RequestContext> resultList = new ArrayList<RequestContext>();
//
//        for (int i = 0;i < requestContextList.size();i++) {
//            RequestContext context = requestContextList.get(i);
//            if (filter.filter(context)) {
//                resultList.add(context);
//            }
//        }
//
        for (RequestContext context : requestContextList) {
            if (filter.filter(context)) {
                resultList.add(context);
            }
        }
        return resultList;
    }

    public void sendRequest(RequestContext context) {
        if ((context.getFlags() & RequestContext.FLAG_CHECK_CONNECT_STATUS) == 0
                || ((context.getFlags() & RequestContext.FLAG_CHECK_CONNECT_STATUS) != 0 && bizSocket.isConnected())) {
            //Logger.e("connected , send request ...");
            //已连接发送请求
            if (sendPacket(context.getRequestPacket())) {
                context.setFlags(context.getFlags() | RequestContext.FLAG_REQUEST_ALREADY_SEND);
                onPacketSend(context);

                if (context.getResponseHandler() == null) {
                    removeRequestContext(context);
                }
            }
        }
        else {
            //等待连接成功后发送
            logger.debug("connect closed ,wait ... ");
        }
    }

    public boolean sendPacket(Packet requestPacket) {
        if (bizSocket.getSocketConnection() != null) {
            bizSocket.getSocketConnection().sendPacket(requestPacket);
            return true;
        }

        return false;
    }

    public void onPacketSend(RequestContext context) {

    }

    //获取串行context
    private AbstractSerialContext getSerialContext(RequestContext context) {
        Packet packet = context.getRequestPacket();
        if (packet != null) {
            for (AbstractSerialContext serialContext : mSerialContexts) {
                if (serialContext.getSerialSignal().getEntranceCommand() == context.getRequestCommand()
                        && serialContext.getRequestPacketId() != null
                        && serialContext.getRequestPacketId().equals(packet.getPacketID())
                        && !serialContext.isExpired()) {
                    return serialContext;
                }
            }
        }
        return null;
    }

    //创建串行context
    public AbstractSerialContext buildSerialContext(SerialSignal serialSignal, RequestContext context) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<? extends AbstractSerialContext> serialContexType = serialSignal.getSerialContextType();
        AbstractSerialContext serialContex = serialContexType.getConstructor(SerialSignal.class,RequestContext.class).newInstance(serialSignal,context);
        return serialContex;
    }

    //移除过期的上线文
    private void removeExpiredSerialContexts() {
        List<AbstractSerialContext> preDelList = new ArrayList<AbstractSerialContext>();
        for (AbstractSerialContext serialContext : mSerialContexts) {
            if (serialContext != null && serialContext.isExpired()) {
                preDelList.add(serialContext);
            }
        }

        mSerialContexts.removeAll(preDelList);

        //Log.e(TAG, "serialContexts remove: " + preDelList);
    }

    /**
     * 根据入口命令获取串行信号
     *
     * @param entranceCommand
     * @return
     */
    private SerialSignal getSerialSignal(Integer entranceCommand) {
        if (entranceCommand != null) {
            for (SerialSignal signal : serialSignalList) {
                if (signal.getEntranceCommand() == entranceCommand) {
                    return signal;
                }
            }
        }
        return null;
    }

    //获取串行上下文
    private AbstractSerialContext getSerialContext(Packet responsePacket) {
//        for (int i = 0; i < mSerialContexts.size(); i++) {
//            AbstractSerialContext serialContext = mSerialContexts.get(i);
//            if (serialContext != null && serialContext.shouldProcess(this,responsePacket)) {
//                return serialContext;
//            }
//        }

        for (AbstractSerialContext serialContext : mSerialContexts) {
            if (serialContext != null && serialContext.shouldProcess(this,responsePacket)) {
                return serialContext;
            }
        }
        return null;
    }

    public void addSerialSignal(SerialSignal serialSignal) {
        serialSignalList.add(serialSignal);
    }

    /**
     * 分发数据包
     * @param responsePacket
     */
    public void dispatchPacket(final Packet responsePacket) {
        responsePacket.onDispatch();
        final int command = responsePacket.getCommand();
        final String packetID = responsePacket.getPacketID() == null ? "" : responsePacket.getPacketID();
        Collection<RequestContext> relativeContexts = getRequestContext(new Filter() {
            @Override
            public boolean filter(RequestContext context) {
                return command == context.getRequestCommand()
                        && packetID.equals(context.getRequestPacket().getPacketID());
            }
        });

        for (RequestContext context : relativeContexts) {
            context.sendSuccessMessage(command,null,responsePacket);
        }
        removeRequestContexts(relativeContexts);

        if (globalNotifyHandler != null) {
            globalNotifyHandler.sendSuccessMessage(command,null,responsePacket);
        }

        recyclePacket(responsePacket);
    }

    /**
     * 执行队列中的所有请求
     */
    public void executeAllRequestContext() {
        Collection<RequestContext> prepareExecuteList = getRequestContext(new Filter() {
            @Override
            public boolean filter(RequestContext context) {
                //获取没有被发送出去的请求
                return (context.getFlags() & RequestContext.FLAG_REQUEST_ALREADY_SEND) == 0;
            }
        });
        for (RequestContext context : prepareExecuteList) {
            sendRequest(context);
        }
    }

    @Override
    public void connected(SocketConnection connection) {
        executeAllRequestContext();
    }

    @Override
    public void connectionClosed() {

    }

    @Override
    public void connectionClosedOnError(Exception e) {

    }

    @Override
    public void reconnectingIn(int seconds) {

    }

    @Override
    public void onSendSuccessful(Packet packet) {
        packet.onSendSuccessful();
    }

    public boolean prepareDispatchPacket(Packet packet) {
        AbstractSerialContext serialContext = getSerialContext(packet);
        if (serialContext != null) {
            logger.debug("about serial packet: " + packet);
            Packet processPacket = serialContext.processPacket(this,packet);
            if (processPacket == null) {
                return false;
            }

            boolean result = mSerialContexts.remove(serialContext);
            if (result) {
                logger.debug("serialContext remove: " + serialContext);
            }
        }
        return true;
    }

    @Override
    public void processPacket(Packet packet) {
        if (packet == null) {
            return;
        }
        try {
            packet.onReceiveFromServer();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (prepareDispatchPacket(packet)) {
            boolean intercepted = getInterceptorChain().invokePesponseHandle(packet.getCommand(),packet);
            if (intercepted) {
                recyclePacket(packet);
                return;
            }
            dispatchPacket(packet);
        }
    }

    public AbstractBizSocket getBizSocket() {
        return bizSocket;
    }

    public InterceptorChain getInterceptorChain() {
        return interceptorChain;
    }

    public void setGlobalNotifyHandler(ResponseHandler globalNotifyHandler) {
        this.globalNotifyHandler = globalNotifyHandler;
    }

    public void cancel(final Object tagOrResponseHandler) {
        Collection<RequestContext> requestContexts = getRequestContext(new RequestQueue.Filter() {
            @Override
            public boolean filter(RequestContext context) {
                return context.getTag() == tagOrResponseHandler || context.getResponseHandler() == tagOrResponseHandler;
            }
        });

        Collection<AbstractSerialContext> abstractSerialContexts = new ArrayList<>();
        for (RequestContext context : requestContexts) {
            for (AbstractSerialContext serialContext : mSerialContexts) {
                if (context == serialContext.getRequestContext()) {
                    abstractSerialContexts.add(serialContext);
                    serialContext.setExpired(true);
                }
            }
        }
        removeRequestContexts(requestContexts);
        mSerialContexts.removeAll(abstractSerialContexts);
    }

    public interface Filter {
        boolean filter(RequestContext context);
    }
}
