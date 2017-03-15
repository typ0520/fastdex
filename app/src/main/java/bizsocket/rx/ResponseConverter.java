package bizsocket.rx;

import bizsocket.tcp.Packet;
import okio.ByteString;
import java.lang.reflect.Type;

/**
 * Created by tong on 16/11/4.
 */
public interface ResponseConverter {
    Object convert(int command, ByteString requestBody, Type type, Packet packet);
}
