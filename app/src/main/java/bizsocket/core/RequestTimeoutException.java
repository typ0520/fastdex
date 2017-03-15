package bizsocket.core;

import java.net.SocketTimeoutException;

/**
 * Created by tong on 16/3/13.
 */
public class RequestTimeoutException extends SocketTimeoutException {
    public RequestTimeoutException() {
    }

    public RequestTimeoutException(String detailMessage) {
        super(detailMessage);
    }
}
