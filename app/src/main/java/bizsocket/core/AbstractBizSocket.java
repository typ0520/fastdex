package bizsocket.core;

import bizsocket.core.cache.CacheManager;
import bizsocket.tcp.*;
import okio.ByteString;

/**
 * Created by tong on 16/10/4.
 */
public abstract class AbstractBizSocket implements Connection,BizSocket {
    protected Configuration configuration;
    protected final SocketConnection socketConnection;
    protected final RequestQueue requestQueue;
    protected final One2ManyNotifyRouter one2ManyNotifyRouter;
    protected final CacheManager cacheManager;

    protected abstract PacketFactory createPacketFactory();

    public AbstractBizSocket() {
        this(null);
    }

    public AbstractBizSocket(Configuration configuration) {
        this.configuration = configuration;
        socketConnection = createSocketConnection(createPacketFactory());
        one2ManyNotifyRouter = createMultiNotifyRouter();
        requestQueue = createRequestQueue(this);
        requestQueue.setGlobalNotifyHandler(new ResponseHandler() {
            @Override
            public void sendSuccessMessage(int command, ByteString requestBody, Packet packet) {
                one2ManyNotifyRouter.route(command, packet);
            }

            @Override
            public void sendFailureMessage(int command, Throwable error) {

            }
        });
        cacheManager = createCacheManager();
    }

    @Override
    public void connect() throws Exception {
        socketConnection.setHostAddress(configuration.getHost(),configuration.getPort());
        socketConnection.connect();
    }

    @Override
    public void disconnect() {
        socketConnection.disconnect();
    }

    @Override
    public boolean isConnected() {
        return socketConnection.isConnected();
    }

    @Override
    public Object request(Request request, ResponseHandler responseHandler) {
        if (request == null) {
            throw new IllegalArgumentException("request can not be null!");
        }
        if (request.tag() == null) {
            request = request.newBuilder().tag(new Object()).build();
        }
        RequestContext requestContext = buildRequestContext(request,responseHandler);
        requestQueue.addRequestContext(requestContext);
        return request.tag();
    }

    @Override
    public void cancel(final Object tagOrResponseHandler) {
        requestQueue.cancel(tagOrResponseHandler);
    }

    @Override
    public void subscribe(Object tag, int cmd, ResponseHandler responseHandler) {
        one2ManyNotifyRouter.subscribe(tag,cmd,responseHandler);
    }

    @Override
    public void unsubscribe(Object tagOrResponseHandler) {
        one2ManyNotifyRouter.unsubscribe(tagOrResponseHandler);
    }

    public SocketConnection createSocketConnection(final PacketFactory factory) {
        return new SocketConnection() {
            @Override
            protected PacketFactory createPacketFactory() {
                return factory;
            }

            @Override
            public void doReconnect(SocketConnection connection) {
                AbstractBizSocket.this.doReconnect();
            }
        };
    }

    public One2ManyNotifyRouter createMultiNotifyRouter() {
        return new DefaultOne2ManyNotifyRouter();
    }

    public RequestQueue createRequestQueue(AbstractBizSocket bizSocket) {
        return new RequestQueue(bizSocket);
    }

    public CacheManager createCacheManager() {
        return new CacheManager(this);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public PacketFactory getPacketFactory() {
        return socketConnection.getPacketFactory();
    }

    public SocketConnection getSocketConnection() {
        return socketConnection;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void addSerialSignal(SerialSignal serialSignal) {
        getRequestQueue().addSerialSignal(serialSignal);
    }

    public InterceptorChain getInterceptorChain() {
        return getRequestQueue().getInterceptorChain();
    }

    public void doReconnect() {
        getSocketConnection().reconnect();
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public One2ManyNotifyRouter getOne2ManyNotifyRouter() {
        return one2ManyNotifyRouter;
    }

    protected RequestContext buildRequestContext(Request request, ResponseHandler responseHandler) {
        Packet requestPacket = getPacketFactory().getRequestPacket(request);
        String desc = request.description();
        if (desc != null && desc.trim().length() > 0) {
            requestPacket.setDescription(desc);
        }
        RequestContext requestContext = new RequestContext(request,requestPacket,responseHandler);
        requestContext.setFlags(RequestContext.FLAG_CHECK_CONNECT_STATUS);
        requestContext.setReadTimeout(configuration.getReadTimeout());
        return requestContext;
    }
}

