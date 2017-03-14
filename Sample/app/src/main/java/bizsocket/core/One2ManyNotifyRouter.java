package bizsocket.core;

import bizsocket.tcp.Packet;

/**
 * Created by tong on 16/10/4.
 */
public interface One2ManyNotifyRouter {
    /**
     * 添加对粘性通知的支持
     * @param cmd
     * @param triggerPacketValidator    用于校验将要被缓存的包的有效性
     */
    void addStickyCmd(int cmd,PacketValidator triggerPacketValidator);

    /**
     * 移除粘性广播命令
     * @param cmd
     */
    void removeStickyCmd(int cmd);

    /**
     * 订阅事件
     * @param tag
     * @param cmd
     * @param responseHandler
     */
    void subscribe(Object tag,int cmd,ResponseHandler responseHandler);

    /**
     * 取消订阅
     * @param tagOrResponseHandler
     */
    void unsubscribe(Object tagOrResponseHandler);

    /**
     * 路由
     * @param command
     * @param packet
     */
    void route(int command, Packet packet);
}
