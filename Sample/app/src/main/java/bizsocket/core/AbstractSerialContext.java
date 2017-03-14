package bizsocket.core;

import bizsocket.tcp.Packet;

/**
 * Created by tong on 16/9/27.
 */
public abstract class AbstractSerialContext {
    //上下文60秒过期
    private static final long EXPIRED_MILLIS = 60 * 1000;
    private long createMillis;
    private final SerialSignal serialSignal;
    private final RequestContext requestContext;
    private boolean expired;

    public AbstractSerialContext(SerialSignal serialSignal, RequestContext requestContext) {
        createMillis = System.currentTimeMillis();
        this.serialSignal = serialSignal;
        this.requestContext = requestContext;
    }

    public SerialSignal getSerialSignal() {
        return serialSignal;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    //是否过期
    public boolean isExpired() {
        return expired || System.currentTimeMillis() - createMillis >= EXPIRED_MILLIS;
    }

    /**
     * 获取入口请求的业务序列号
     * @return
     */
    public String getRequestPacketId() {
        if (requestContext != null && requestContext.getRequestPacket() != null) {
            return requestContext.getRequestPacket().getPacketID();
        }
        return null;
    }

    /**
     * 是否分发这个包
     * @param packet
     * @return 不为null 继续分发  为null 不分发
     */
    public abstract Packet processPacket(RequestQueue requestQueue, Packet packet);

    //是否属于这个上下文的包
    public boolean shouldProcess(RequestQueue requestQueue,Packet packet) {
        int command = packet.getCommand();
        String requestPacketId = getRequestPacketId();
        String currentPacketId = packet.getPacketID();
        return (requestPacketId != null && requestPacketId.equals(currentPacketId)) || (serialSignal.isStrongReference(command) || serialSignal.isWeekReference(command));
    }
}
