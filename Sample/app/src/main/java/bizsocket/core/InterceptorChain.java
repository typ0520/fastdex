package bizsocket.core;

import bizsocket.tcp.Packet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by tong on 16/3/7.
 */
public class InterceptorChain {
    private final List<Interceptor> interceptors = Collections.synchronizedList(new ArrayList<Interceptor>());

    public void addInterceptor(Interceptor interceptor) {
        if (!interceptors.contains(interceptor)) {
            interceptors.add(interceptor);
        }
    }

    public void removeInterceptor(Interceptor interceptor) {
        interceptors.remove(interceptor);
    }

    public boolean invokePostRequestHandle(RequestContext context){
        for (Interceptor interceptor : interceptors) {
            try {
                if (interceptor.postRequestHandle(context)) {
                    //拦截请求
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean invokePesponseHandle(int command, Packet responsePacket) {
        for (Interceptor interceptor : interceptors) {
            try {
                if (interceptor.postResponseHandle(command,responsePacket)) {
                    //拦截响应
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
