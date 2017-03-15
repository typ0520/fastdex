package bizsocket.rx;

import okio.ByteString;
import java.lang.reflect.Method;

/**
 * Created by tong on 16/11/4.
 */
public interface RequestConverter {
    ByteString converter(Method method, Object... args);
}
