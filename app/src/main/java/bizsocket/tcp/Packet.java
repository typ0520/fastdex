package bizsocket.tcp;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;

/**
 * Base class for tcp packets. Every packet has a unique ID (which is automatically
 * generated, but can be overridden).
 */
public abstract class Packet {
    private static final Logger LOGGER = LoggerFactory.getLogger(Packet.class.getSimpleName());

    /**
     * 可被回收复用的
     */
    public static final int FLAG_RECYCLABLE = 1;

    /**
     * 刚发送成功后自动回收
     */
    public static final int FLAG_AUTO_RECYCLE_ON_SEND_SUCCESS = 1 << 1;

    /**
     * 处于被回收状态
     */
    public static final int FLAG_RECYCLED = 1 << 2;

    private int command;
    private String description;
    private int flags = FLAG_RECYCLABLE;
    private PacketPool packetPool;

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Returns the packet as bytes.
     */
    public abstract byte[] toBytes();

    public abstract String getContent();

    /**
     * Returns the unique ID of the packet.
     */
    public abstract String getPacketID();

    public abstract void setPacketID(String packetID);

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取包的描述
     * @return
     */
    public String getDescription() {
        return this.description;
    }

    public void onSendSuccessful() {
        LOGGER.debug("-------------------send packet: " + getCommand() + " ,desc: " + getDescription() + ", id: " + getPacketID());
        LOGGER.debug("-------------------send content: " + getContent());
    }

    public void onReceiveFromServer() {

    }

    public void onDispatch() {
        LOGGER.debug("-------------------receive: cmd: " + getCommand() + ", id: " + getPacketID() + " ,desc: " + getDescription());
        LOGGER.debug("-------------------receive: content: " + getContent());
    }

    void setPacketPool(PacketPool packetPool) {
        this.packetPool = packetPool;
    }

    /**
     * 准备复用时调用
     */
    public void onPrepareReuse() {
        LOGGER.debug("prepare reuse： " + toString());
    }

    public void onRecycle() {
        LOGGER.debug("packet recycled： " + toString());
    }

    /**
     * 回收packet
     */
    public void recycle() {
        if ((getFlags() & FLAG_RECYCLABLE) == 0) {
            return;
        }
        if ((getFlags() & FLAG_RECYCLED) != 0) {
            return;
        }

        if (packetPool != null) {
            setFlags(getFlags() | FLAG_RECYCLED);
            packetPool.push(this);
        }
        onRecycle();
    }

    @Override
    public String toString() {
        return "Packet{" +
                "command=" + command +
                ", description='" + description + '\'' +
                ", content" + getContent() +
                '}';
    }
}