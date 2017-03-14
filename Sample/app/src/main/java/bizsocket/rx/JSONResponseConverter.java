package bizsocket.rx;

import bizsocket.tcp.Packet;
import okio.ByteString;
import com.google.gson.Gson;
import org.json.JSONObject;
import java.lang.reflect.Type;

/**
 * Created by tong on 16/10/6.
 */
public class JSONResponseConverter implements ResponseConverter {
    @Override
    public Object convert(int command, ByteString requestBody, Type type, Packet packet) {
        if (type == String.class) {
            return packet.getContent();
        }
        else if (type == JSONObject.class) {
            try {
                return new JSONObject(packet.getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            return new Gson().fromJson(packet.getContent(),type);
        }
        return null;
    }
}
