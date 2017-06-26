package fastdex.build.lib.snapshoot.api;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by tong on 17/3/30.
 */
public interface STSerializable {
    void serializeTo(OutputStream outputStream) throws IOException;
}
