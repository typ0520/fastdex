package bizsocket.core;

import bizsocket.tcp.Packet;

/**
 * Created by tong on 16/10/6.
 */
public interface PacketValidator {
    /**
     * 校验包的有效性
     * @param packet
     * @return true 有效  false 无效
     */
    boolean verify(Packet packet);
}
