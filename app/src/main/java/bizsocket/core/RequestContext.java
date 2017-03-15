package bizsocket.core;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import bizsocket.tcp.Packet;
import bizsocket.tcp.Request;
import okio.ByteString;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tong on 16/3/7.
 */
public class RequestContext implements ResponseHandler {
    /**
     * 请求已发送
     */
    public static final int FLAG_REQUEST_ALREADY_SEND = 1 << 1;

    /**
     * 发送时是否检查当前连接状态
     */
    public static final int FLAG_CHECK_CONNECT_STATUS = 1 << 2;

    /**
     * 紧急的包需要优先插队执行
     */
    public static final int FLAG_JUMP_QUEUE = 1 << 3;

    /**
     * 写出这个包之前清空队列
     */
    public static final int FLAG_CLEAR_QUEUE = 1 << 4;

    /**
     * 同一个请求不允许重复出现在队列中
     */
    public static final int FLAG_NOT_SUPPORT_REPEAT = 1 << 5;

    private final Logger logger = LoggerFactory.getLogger(RequestContext.class.getSimpleName());

    private final Request request;
    /**
     * 请求包
     */
    private Packet requestPacket;
    /**
     * callback
     */
    private final ResponseHandler responseHandler;

    /**
     * 状态机
     */
    private int flags = FLAG_CHECK_CONNECT_STATUS;
    private OnRequestTimeoutListener onRequestTimeoutListener;
    private Timer timer;
    private long readTimeout = Configuration.DEFAULT_READ_TIMEOUT;

    public RequestContext(Request request, Packet requestPacket, ResponseHandler responseHandler) {
        this.request = request;
        this.requestPacket = requestPacket;
        this.responseHandler = responseHandler;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public Object getTag() {
        return request.tag();
    }

    public Packet getRequestPacket() {
        return requestPacket;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public int getRequestCommand() {
        return request.command();
    }

    public void setOnRequestTimeoutListener(OnRequestTimeoutListener listener) {
        this.onRequestTimeoutListener = listener;
    }

    @Override
    public void sendSuccessMessage(int command, ByteString requestBody, Packet packet) {
        if (responseHandler != null) {
            responseHandler.sendSuccessMessage(command, this.request.body(), packet);
        }
    }

    @Override
    public void sendFailureMessage(int command, Throwable error) {
        if (responseHandler != null) {
            responseHandler.sendFailureMessage(command, error);
        }
    }

    public void startTimeoutTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                RequestContext.this.onRequestTimeoutListener.onRequestTimeout(RequestContext.this);
            }
        },readTimeout * 1000);
    }

    public ByteString getRequestBody() {
        return request.body();
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void onAddToQueue() {
        startTimeoutTimer();
    }

    public void onRemoveFromQueue() {
        logger.debug("remove from queue: " + toString());
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public Map getAttach() {
        return request.attach();
    }

    public Request getRequest() {
        return request;
    }

    public void setRequestPacket(Packet requestPacket) {
        this.requestPacket = requestPacket;
    }

    @Override
    public String toString() {
        return "RequestContext{" +
                "request=" + request +
                ", responseHandler=" + responseHandler +
                '}';
    }

    public interface OnRequestTimeoutListener {
        void onRequestTimeout(RequestContext context);
    }
}
