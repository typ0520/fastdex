package bizsocket.tcp;

public interface PacketListener {
    /**
     * Call this method when the packet send successful
     * @param packet
     */
    void onSendSuccessful(Packet packet);

    /**
     * Process the next packet sent to this packet listener.<p>
     *
     * A single thread is responsible for invoking all listeners, so
     * it's very important that implementations of this method not block
     * for any extended period of time.
     *
     * @param packet the packet to process.
     */
    void processPacket(Packet packet);
}
