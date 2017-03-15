package bizsocket.core.cache;

import bizsocket.core.Interceptor;
import bizsocket.core.PacketValidator;
import bizsocket.core.RequestContext;
import bizsocket.tcp.Packet;

/**
 * 接收指定的的命令后移除缓存
 * Created by tong on 16/10/21.
 */
public class UseUtilReceiveCmdCacheStrategy extends CacheStrategy implements Interceptor {

    private int[] conflictCommands;
    private PacketValidator triggerPacketValidator;

    public UseUtilReceiveCmdCacheStrategy(int command, int[] conflictCommands) {
        this(command,conflictCommands,null);
    }

    public UseUtilReceiveCmdCacheStrategy(int command, int[] conflictCommands,PacketValidator validator) {
        super(command, validator);
        this.conflictCommands = conflictCommands;
        if (conflictCommands == null || conflictCommands.length == 0) {
            throw new IllegalArgumentException("conflict commands can not be null or empty");
        }
    }

    public void setTriggerPacketValidator(PacketValidator receivePacketValidator) {
        this.triggerPacketValidator = receivePacketValidator;
    }

    @Override
    public void onMount(CacheManager cacheManager) {
        super.onMount(cacheManager);

        cacheManager.getBizSocket().getInterceptorChain().addInterceptor(this);
    }

    @Override
    public void onUnmount(CacheManager cacheManager) {
        super.onUnmount(cacheManager);

        cacheManager.getBizSocket().getInterceptorChain().removeInterceptor(this);
    }

    public void processTriggerPacket(Packet packet) {
        if (packet == null) {
            return;
        }

        int command = packet.getCommand();

        for (int cmd : conflictCommands) {
            if (cmd == command) {
                if (triggerPacketValidator == null) {
                    removeCache();
                }
                else {
                    if (triggerPacketValidator.verify(packet)) {
                        logger.debug("prepare remove cache, receive packet verify success: " + packet);
                        removeCache();
                    }
                    else {
                        logger.debug("ignore remove event receive packet verify fail: " + packet);
                    }
                }
                break;
            }
        }
    }

    @Override
    public boolean postRequestHandle(RequestContext context) throws Exception {
        return false;
    }

    @Override
    public boolean postResponseHandle(int command, Packet responsePacket) throws Exception {
        processTriggerPacket(responsePacket);
        return false;
    }
}
