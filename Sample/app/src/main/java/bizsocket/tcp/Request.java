package bizsocket.tcp;

import okio.ByteString;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tong on 16/10/19.
 */
public final class Request {
    private Object tag;
    private int command;
    private ByteString body;
    private Map attach;
    private String description;
    private boolean recycleOnSend;//自动回收请求包

    public Object tag() {
        return tag;
    }

    public int command() {
        return command;
    }

    public ByteString body() {
        return body;
    }

    public Map attach() {
        return attach;
    }

    public String description() {
        return description;
    }

    public boolean recycleOnSend() {
        return recycleOnSend;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public void putToAttach(Object key,Object value) {
        if (attach == null) {
            attach = new HashMap();
        }
        attach.put(key, value);
    }

    public Object getFromAttach(Object key) {
        if (attach != null) {
            return attach.get(key);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Request{" +
                "command=" + command +
                ", body=" + body +
                ", description='" + description + '\'' +
                '}';
    }

    public static class Builder {
        private final Request request;

        public Builder() {
            this.request = new Request();
        }

        public Builder(Request req) {
            this.request = new Request();
            this.request.tag = req.tag();
            this.request.command = req.command();
            this.request.body = req.body();
            this.request.attach = req.attach();
        }

        public Builder tag(Object tag) {
            this.request.tag = tag;
            return this;
        }

        public Builder command(int command) {
            this.request.command = command;
            return this;
        }

        public Builder body(ByteString body) {
            this.request.body = body;
            return this;
        }

        public Builder utf8body(String utf8body) {
            this.request.body = ByteString.encodeUtf8(utf8body);
            return this;
        }

        public Builder attach(Map attach) {
            this.request.attach = attach;
            return this;
        }

        public Builder description(String description) {
            this.request.description = description;
            return this;
        }

        public Builder recycleOnSend(boolean recycleOnSend) {
            this.request.recycleOnSend = recycleOnSend;
            return this;
        }

        public Request build() {
            return request;
        }
    }
}
