package bizsocket.core.cache;

import bizsocket.core.PacketValidator;
import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import bizsocket.tcp.Packet;

/**
 * Created by tong on 16/10/5.
 */
public class CacheStrategy {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private int command;
    private Packet packet;
    private PacketValidator validator;

    public CacheStrategy(int command) {
        this.command = command;
    }

    public CacheStrategy(int command, PacketValidator validator) {
        this.command = command;
        this.validator = validator;
    }

    //当策略被挂载
    public void onMount(CacheManager cacheManager) {

    }

    //当策略被卸载
    public void onUnmount(CacheManager cacheManager) {

    }

    //当缓存被命中
    public void onHit() {

    }

    //当缓存被移除时调用
    public  void onRemoveCache() {

    }

    //当更新缓存时调用
    public void onUpdateCache(Packet networkPacket) {

    }

    /**
     * 更新缓存
     * @param networkPacket
     */
    public final void updateCache(Packet networkPacket) {
        if (networkPacket == null) {
            return;
        }
        if (command != networkPacket.getCommand()) {
            throw new IllegalArgumentException("can not update packet, expect cmd: " + command + " but param cmd is " + networkPacket.getCommand());
        }
        if (validator != null && !validator.verify(networkPacket)) {
            logger.debug("ignore cache; check fail packet: " + networkPacket);
            return;
        }

        this.packet = networkPacket;
        //被缓存的包不可复用
        networkPacket.setFlags(networkPacket.getFlags() & ~Packet.FLAG_RECYCLABLE);
        logger.debug("save or update cache packet: " + packet);

        onUpdateCache(networkPacket);
    }

    public int getCommand() {
        return command;
    }

    /**
     * 获取可用的缓存
     * @return
     */
    public Packet getValidCache() {
        return packet;
    }

    /**
     * 移除缓存
     */
    public synchronized void removeCache() {
        onRemoveCache();
        this.packet = null;
    }

    public void setValidator(PacketValidator validator) {
        this.validator = validator;
    }

    void setPacket(Packet packet) {
        this.packet = packet;
    }
}
