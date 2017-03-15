package bizsocket.core;

import bizsocket.tcp.Packet;

/**
 * socket请求响应拦截器
 * Created by tong on 16/2/18.
 */
public interface Interceptor {
    /**
     * 决定是否拦截请求
     * @return true 拦截请求
     * @throws Exception
     */
    boolean postRequestHandle(RequestContext context) throws Exception;

    /**
     * 决定是否拦截响应
     * @return true 拦截响应
     * @throws Exception
     */
    boolean postResponseHandle(int command, Packet responsePacket) throws Exception;
}
