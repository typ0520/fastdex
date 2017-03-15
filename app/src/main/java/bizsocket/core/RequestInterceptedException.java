package bizsocket.core;

/**
 * Created by tong on 16/10/5.
 */
public class RequestInterceptedException extends RuntimeException {
    public RequestInterceptedException() {
    }

    public RequestInterceptedException(String detailMessage) {
        super(detailMessage);
    }
}
