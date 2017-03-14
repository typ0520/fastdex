package bizsocket.core.cache;

import bizsocket.core.PacketValidator;
import bizsocket.tcp.Packet;
import java.util.concurrent.TimeUnit;

/**
 * 按相对时间移除缓存
 * Created by tong on 16/10/21.
 */
public class RelativeMillisCacheStrategy extends CacheStrategy {
    private long dMillis;
    private long expiredMillis;

    public RelativeMillisCacheStrategy(int command, TimeUnit unit, long duration) {
        this(command,unit,duration,null);
    }

    public RelativeMillisCacheStrategy(int command, TimeUnit unit, long duration,PacketValidator validator) {
        super(command, validator);
        dMillis = unit.toMillis(duration);
        expiredMillis = System.currentTimeMillis() + dMillis;
    }

    @Override
    public Packet getValidCache() {
        if (System.currentTimeMillis() > expiredMillis) {
            removeCache();
        }
        return super.getValidCache();
    }

    @Override
    public void onUpdateCache(Packet networkPacket) {
        expiredMillis = System.currentTimeMillis() + dMillis;
    }
}
