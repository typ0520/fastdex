package bizsocket.core;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import bizsocket.tcp.Packet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by tong on 16/10/4.
 */
public class DefaultOne2ManyNotifyRouter implements One2ManyNotifyRouter {
    private final Logger logger = LoggerFactory.getLogger(DefaultOne2ManyNotifyRouter.class.getSimpleName());

    private final Collection<NotifyContext> notifyContexts = new CopyOnWriteArrayList<NotifyContext>();
    private final Map<Integer,Packet> packetMap = new ConcurrentHashMap<>();
    private final Set<StickyContext> stickyCmds = Collections.synchronizedSet(new HashSet<StickyContext>());

    /**
     * 添加对粘性通知的支持
     * @param cmd
     */
    @Override
    public void addStickyCmd(int cmd,PacketValidator triggerPacketValidator) {
        stickyCmds.add(new StickyContext(cmd, triggerPacketValidator));
    }

    /**
     * 移除粘性广播命令
     * @param command
     */
    @Override
    public void removeStickyCmd(int command) {
        StickyContext stickyContext = null;
        for (StickyContext context : stickyCmds) {
            if (context.cmd == command) {
                stickyContext = context;
                break;
            }
        }
        if (stickyContext != null) {
            stickyCmds.remove(stickyContext);
            packetMap.remove(command);
        }
    }

    @Override
    public void subscribe(Object tag, int cmd, ResponseHandler responseHandler) {
        if (tag == null || responseHandler == null) {
            return;
        }
        NotifyContext notifyContext = new NotifyContext(tag,cmd,responseHandler);
        notifyContexts.add(notifyContext);

        Packet packet = null;
        if (stickyCmds.contains(cmd) && (packet = packetMap.get(cmd)) != null) {
            //如果是粘性广播命令并且有缓存的包，立即回调一次
            logger.debug("Sticky callback: " + packet);
            sendSuccessMessage(notifyContext,cmd,packet);
        }
    }

    @Override
    public void unsubscribe(Object tagOrResponseHandler) {
        if (tagOrResponseHandler == null) {
            return;
        }
        List<NotifyContext> preDelList = new ArrayList<NotifyContext>();
        for (NotifyContext notifyContext : notifyContexts) {
            if (notifyContext.tag == tagOrResponseHandler || notifyContext.responseHandler == tagOrResponseHandler) {
                preDelList.add(notifyContext);
            }
        }

        notifyContexts.removeAll(preDelList);
    }

    @Override
    public void route(int command, Packet packet) {
        if (packet == null || packet.getCommand() != command) {
            logger.error("can not route command: " + command + " packet: " + packet);
            return;
        }
        for (NotifyContext notifyContext : notifyContexts) {
            if (notifyContext.cmd == command) {
                sendSuccessMessage(notifyContext,command,packet);
            }
        }

        StickyContext stickyContext = null;
        for (StickyContext context : stickyCmds) {
            if (context.cmd == command) {
                stickyContext = context;
                break;
            }
        }
        if (stickyContext != null
                && stickyContext.triggerPacketValidator != null
                && stickyContext.triggerPacketValidator.verify(packet)) {
            packet.setFlags(packet.getFlags() | Packet.FLAG_RECYCLABLE);
            packetMap.put(packet.getCommand(),packet);
        }
    }

    public void sendSuccessMessage(NotifyContext notifyContext, int command, Packet packet) {
        notifyContext.sendSuccessMessage(command, packet);
    }

    private static class StickyContext {
        int cmd;
        PacketValidator triggerPacketValidator;

        public StickyContext(int cmd, PacketValidator triggerPacketValidator) {
            this.cmd = cmd;
            this.triggerPacketValidator = triggerPacketValidator;

            if (this.triggerPacketValidator == null) {
                throw new IllegalArgumentException("triggerPacketValidator can not be null");
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StickyContext that = (StickyContext) o;

            return cmd == that.cmd;

        }

        @Override
        public int hashCode() {
            return cmd;
        }
    }

    private static class NotifyContext {
        int cmd;
        Object tag;
        ResponseHandler responseHandler;

        public NotifyContext(Object tag,int cmd,ResponseHandler responseHandler) {
            this.cmd = cmd;
            this.tag = tag;
            this.responseHandler = responseHandler;
        }

        public void sendSuccessMessage(int command, Packet packet) {
            try {
                responseHandler.sendSuccessMessage(command, null, packet);
            } catch (Throwable e) {

            }
        }
    }
}
