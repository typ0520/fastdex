package fastdex.runtime;

/**
 * Created by tong on 17/4/24.
 */
public class FastdexRuntimeException extends RuntimeException {
    public FastdexRuntimeException(String detailMessage) {
        super(detailMessage);
    }

    public FastdexRuntimeException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public FastdexRuntimeException(Throwable throwable) {
        super(throwable);
    }
}

