package bizsocket.rx;

import bizsocket.core.BizSocket;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by tong on 16/10/6.
 */
public class BizSocketRxSupport {
    private final BizSocket bizSocket;
    private final RequestConverter requestConverter;
    private final ResponseConverter responseConverter;

    BizSocketRxSupport(BizSocket bizSocket, RequestConverter requestConverter, ResponseConverter responseConverter) {
        this.responseConverter = responseConverter;
        this.bizSocket = bizSocket;
        this.requestConverter = requestConverter;
    }

    public BizSocket getBizSocket() {
        return bizSocket;
    }

    public RequestConverter getRequestConverter() {
        return requestConverter;
    }

    public ResponseConverter getResponseConverter() {
        return responseConverter;
    }

    public <T> T create(final Class<T> service) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("API declarations must be interfaces.");
        }
        // Prevent API interfaces from extending other interfaces. This not only avoids a bug in
        // Android (http://b.android.com/58753) but it forces composition of API declarations which is
        // the recommended pattern.
        if (service.getInterfaces().length > 0) {
            throw new IllegalArgumentException("API interfaces must not extend other interfaces.");
        }

        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object... args)
                            throws Throwable {
                        // If the method is a method from Object then defer to normal invocation.
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }

                        return new BizSocketCall().call(BizSocketRxSupport.this,method,args);
                    }
                });
    }

    public static class Builder {
        private BizSocket bizSocket;
        private RequestConverter requestConverter;
        private ResponseConverter responseConverter;

        public Builder bizSocket(BizSocket bizSocket) {
            this.bizSocket = bizSocket;
            return this;
        }

        public Builder requestConverter(RequestConverter converter) {
            this.requestConverter = converter;
            return this;
        }

        public Builder responseConverter(ResponseConverter converter) {
            this.responseConverter = converter;
            return this;
        }

        public BizSocketRxSupport build() {
            return new BizSocketRxSupport(bizSocket,requestConverter,responseConverter);
        }
    }
}
