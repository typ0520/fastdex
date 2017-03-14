package bizsocket.tcp;

/**
 * Created by tong on 16/10/19.
 */
public interface PacketPool {
    Packet pull();

    void push(Packet packet);
}
