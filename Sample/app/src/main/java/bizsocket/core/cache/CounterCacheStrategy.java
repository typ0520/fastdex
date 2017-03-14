package bizsocket.core.cache;

import bizsocket.core.PacketValidator;
import bizsocket.tcp.Packet;

/**
 * 按使用的次数移除缓存
 * Created by tong on 16/10/21.
 */
public class CounterCacheStrategy extends CacheStrategy {
    private int expiresCount;
    private int count;

    public CounterCacheStrategy(int command, int expiresCount) {
        this(command,expiresCount, null);
    }

    public CounterCacheStrategy(int command, int expiresCount,PacketValidator validator) {
        super(command, validator);
        if (expiresCount <= 0) {
            throw new IllegalArgumentException("expiresCount >= 1,but: " + expiresCount);
        }
        this.expiresCount = expiresCount;
    }

    @Override
    public void onHit() {
        super.onHit();
        count ++;
        if (count >= expiresCount) {
            removeCache();
        }
        logger.debug("expiresCount: " + expiresCount + " current: " + count);
    }

    @Override
    public void onUpdateCache(Packet networkPacket) {
        count = 0;
        logger.debug("reset count expiresCount: " + expiresCount + " current: " + count);
    }

    public int getExpiresCount() {
        return expiresCount;
    }

    public int getCount() {
        return count;
    }
}
