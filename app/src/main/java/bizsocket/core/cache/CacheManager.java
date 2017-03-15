package bizsocket.core.cache;

import bizsocket.core.AbstractBizSocket;
import bizsocket.core.Interceptor;
import bizsocket.core.RequestContext;
import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import bizsocket.tcp.Packet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tong on 16/10/5.
 */
public class CacheManager implements Interceptor {
    private final Logger logger = LoggerFactory.getLogger(CacheManager.class.getSimpleName());
    private final Map<Integer,CacheStrategy> cacheStrategyMap = new ConcurrentHashMap<Integer, CacheStrategy>();
    private final AbstractBizSocket bizSocket;

    public CacheManager(AbstractBizSocket bizSocket) {
        this.bizSocket = bizSocket;
        this.bizSocket.getInterceptorChain().addInterceptor(this);
    }

    /**
     * 获取缓存策略
     * @param cmd
     * @return
     */
    public CacheStrategy get(int cmd) {
        return cacheStrategyMap.get(cmd);
    }

    /**
     * 添加缓存策略
     * @param entry
     */
    public void add(CacheStrategy entry) {
        if (entry != null) {
            entry.onMount(this);
            cacheStrategyMap.put(entry.getCommand(),entry);
        }
    }

    /**
     * 移除缓存策略
     * @param cacheStrategy
     */
    public void remove(CacheStrategy cacheStrategy) {
        if (cacheStrategy != null) {
            cacheStrategy.onUnmount(this);
            remove(cacheStrategy.getCommand());
        }
    }

    /**
     * 通过命令号移除缓存策略
     */
    public void remove(int cmd) {
        cacheStrategyMap.remove(cmd);
    }

    /**
     * 移除所有的缓存策略
     */
    public void removeAll() {
        cacheStrategyMap.clear();
    }

    /**
     * 根据命令号移除缓存
     * @param cmd
     */
    public void removeCache(int cmd) {
        CacheStrategy cacheStrategy = get(cmd);
        if (cacheStrategy != null) {
            cacheStrategy.removeCache();
        }
    }

    /**
     * 移除所有缓存
     */
    public void removeAllCache() {
        for (CacheStrategy cacheStrategy : cacheStrategyMap.values()) {
            cacheStrategy.removeCache();
        }
    }

    public AbstractBizSocket getBizSocket() {
        return bizSocket;
    }

    @Override
    public boolean postRequestHandle(RequestContext context) throws Exception {
        CacheStrategy cacheStrategy = get(context.getRequestCommand());
        Packet cachedPacket = null;
        if (cacheStrategy != null && (cachedPacket = cacheStrategy.getValidCache()) != null) {
            logger.debug("Use cache packet " + cachedPacket);
            context.sendSuccessMessage(context.getRequestCommand(),null, cacheStrategy.getValidCache());
            //命中缓存
            cacheStrategy.onHit();
            return true;
        }
        return false;
    }

    @Override
    public boolean postResponseHandle(int command, Packet responsePacket) throws Exception {
        CacheStrategy cacheStrategy = get(responsePacket.getCommand());
        if (cacheStrategy != null) {
            cacheStrategy.updateCache(responsePacket);
        }
        return false;
    }
}
