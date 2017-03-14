package bizsocket.rx;

import okio.ByteString;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by tong on 16/10/6.
 */
public class JSONRequestConverter implements RequestConverter {
    @Override
    public ByteString converter(Method method, Object... args) {
        try {
            JSONObject obj = new JSONObject();
            Request request = method.getAnnotation(Request.class);
            String queryString = request.queryString();
            if (queryString != null) {
                String[] arr = queryString.split("&");

                for (String str : arr) {
                    if (str != null) {
                        String[] keyVal = str.split("=");
                        if (keyVal != null && keyVal.length == 2) {
                            obj.put(keyVal[0],keyVal[1]);
                        }
                    }
                }
            }

            Annotation[][] annotationArray = method.getParameterAnnotations();
            int index = 0;
            for (Annotation[] annotations : annotationArray) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Query) {
                        Query query = (Query) annotation;
                        try {
                            obj.put(query.value(), args[index]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (annotation instanceof QueryMap) {
                        Object keyValueStore = args[index];
                        if (keyValueStore instanceof Map) {
                            Map map = (Map) keyValueStore;
                            for (Object key : map.keySet()) {
                                try {
                                    obj.put(String.valueOf(key), map.get(key));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (keyValueStore instanceof JSONObject) {
                            JSONObject jobj = (JSONObject) keyValueStore;
                            JSONArray names = jobj.names();
                            if (names != null) {
                                for (int i = 0;i < names.length();i++) {
                                    obj.put(names.optString(i),jobj.opt(names.optString(i)));
                                }
                            }
                        }
                    }

                }
                index ++;
            }
            return ByteString.encodeUtf8(obj.toString());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
