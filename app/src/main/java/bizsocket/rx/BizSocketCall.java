package bizsocket.rx;

import bizsocket.core.ResponseHandler;
import bizsocket.tcp.Packet;
import com.google.gson.internal.$Gson$Types;
import okio.ByteString;
import rx.Observable;
import rx.Subscriber;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by tong on 16/10/6.
 */
public class BizSocketCall {
    public Observable<?> call(final BizSocketRxSupport bizSocketRxSupport, final Method method, Object... args) throws Exception {
        Request request = null;
        if ((request = method.getAnnotation(Request.class)) == null) {
            throw new IllegalArgumentException("Can not found annotation(" + Request.class.getPackage() + "." + Request.class.getSimpleName() + ")");
        }
        final int command = getCommand(request,method,args);
        System.out.print("cmd: " + command);
        final Object tag = getTag(method,args);
        final ByteString requestBody = getRequestBody(bizSocketRxSupport,method,args);

        final String desc = request.desc();
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(final Subscriber<? super Object> subscriber) {
                bizsocket.tcp.Request req = new bizsocket.tcp.Request.Builder().tag(tag).command(command).body(requestBody).description(desc).build();
                bizSocketRxSupport.getBizSocket().request(req, new ResponseHandler() {
                    @Override
                    public void sendSuccessMessage(int command, ByteString requestBody, Packet responsePacket) {
                        Object response = bizSocketRxSupport.getResponseConverter().convert(command,requestBody,getResponseType(method),responsePacket);
                        subscriber.onNext(response);
                    }

                    @Override
                    public void sendFailureMessage(int command, Throwable error) {
                        subscriber.onError(error);
                    }
                });
            }
        });
    }

    public int getCommand(Request request,Method method, Object[] args) {
        int cmd = request.cmd();
        Annotation[][] annotationArray = method.getParameterAnnotations();
        int index = 0;
        for (Annotation[] annotations : annotationArray) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Cmd) {
                    return (Integer)args[index];
                }
            }
            index ++;
        }
        return cmd;
    }

    public Object getDefaultTag() {
        return this;
    }

    public Type getResponseType(Method method) {
        if (method.getReturnType() != Observable.class) {
            throw new IllegalStateException("return type must be rx.Observable");
        }
        Type type = method.getGenericReturnType();
        if (type instanceof ParameterizedType) {
            type = $Gson$Types.canonicalize(((ParameterizedType)type).getActualTypeArguments()[0]);
        }

        if ("rx.Observable".equals(type.toString())) {
            throw new IllegalStateException("the generic value of the return value is unspecified; support " + "rx.Observable<String> | rx.Observable<JSONObject> | rx.Observable<JavaBean>");
        }
        return type;
    }

    public Object getTag(Method method,Object... args) {
        Annotation[][] annotationArray = method.getParameterAnnotations();
        int index = 0;
        for (Annotation[] annotations : annotationArray) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Tag) {
                    return args[index];
                }
            }
            index ++;
        }
        return getDefaultTag();
    }

    public ByteString getRequestBody(BizSocketRxSupport bizSocketRxSupport, Method method, Object[] args) throws Exception {
        return bizSocketRxSupport.getRequestConverter().converter(method, args);
    }
}
