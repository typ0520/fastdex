package bizsocket.tcp;

import java.io.IOException;
import okio.BufferedSource;

/**
 * Created by tong on 16/10/3.
 */
public abstract class PacketFactory {
    private final PacketPool packetPool;

    public PacketFactory() {
        this(new SimplePacketPool());
    }

    public PacketFactory(PacketPool packetPool) {
        this.packetPool = packetPool;
    }

    /**
     * create request packet with command and body
     * @param request
     * @param reusable 可重复利用的packet
     * @return
     */
    public abstract Packet getRequestPacket(Packet reusable,Request request);

    /**
     * create heartbeat packet
     * @param reusable 可重复利用的packet
     * @return
     */
    public abstract Packet getHeartBeatPacket(Packet reusable);

    /**
     * create packet from the stream of server
     * @param reusable 可重复利用的packet
     * @param source
     * @return
     * @throws IOException
     */
    public abstract Packet getRemotePacket(Packet reusable,BufferedSource source) throws IOException;


    public final Packet getRequestPacket(Request request) {
        Packet packet = getRequestPacket(getPacketPool().pull(),request);
        if (packet != null && request.recycleOnSend()) {
            packet.setFlags(packet.getFlags() | Packet.FLAG_AUTO_RECYCLE_ON_SEND_SUCCESS);
        }
        return packet;
    }

    public final Packet getHeartBeatPacket() {
        Packet packet = getHeartBeatPacket(getPacketPool().pull());
        if (packet != null) {
            //自动回收心跳包
            packet.setFlags(packet.getFlags() | Packet.FLAG_AUTO_RECYCLE_ON_SEND_SUCCESS);
        }
        return packet;
    }

    public final Packet getRemotePacket(BufferedSource source) throws IOException {
        return getRemotePacket(getPacketPool().pull(),source);
    }

    public PacketPool getPacketPool() {
        return packetPool;
    }
}
