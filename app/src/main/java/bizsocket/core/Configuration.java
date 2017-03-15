package bizsocket.core;

import java.util.concurrent.TimeUnit;

/**
 * Created by tong on 16/3/7.
 */
public class Configuration {
    public static final int DEFAULT_READ_TIMEOUT = 30;
    public static final int HEART_BEAT_INTERVAL = 20;

    private long readTimeout = DEFAULT_READ_TIMEOUT;
    private String host;//socket server host
    private int port;//端口
    private int heartbeat;//心跳间隔
    private boolean logEnable;
    private String logTag = "SocketClient";
    private Configuration actual;

    Configuration() {
    }

    protected Configuration(int readTimeout, String host, int port, int heartbeat) {
        this.readTimeout = readTimeout;
        this.host = host;
        this.port = port;
        this.heartbeat = heartbeat;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    /**
     * 设置超时时间(单位是秒)
     * @param readTimeout
     */
    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    /**
     * 设置心跳时间间隔(单位是秒)
     * @param heartbeat
     */
    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public boolean isLogEnable() {
        return logEnable;
    }

    public void setLogEnable(boolean logEnable) {
        this.logEnable = logEnable;
    }

    public String getLogTag() {
        return logTag;
    }

    public void setLogTag(String logTag) {
        this.logTag = logTag;
    }

    public void apply(Configuration configuration) {
        if (configuration == null) {
            return;
        }
        this.readTimeout = configuration.getReadTimeout();
        this.host = configuration.getHost();
        this.port = configuration.getPort();
        this.heartbeat = configuration.getHeartbeat();
        this.actual = configuration;
    }

    public Configuration getActual() {
        return actual;
    }

    public static class Builder {
        private Configuration configuration;

        public Builder() {
            this.configuration = new Configuration();
        }

        public Builder readTimeout(TimeUnit unit,long duration) {
            configuration.setReadTimeout(unit.toSeconds(duration));
            return this;
        }

        public Builder host(String host) {
            configuration.setHost(host);
            return this;
        }

        public Builder port(int port) {
            configuration.setPort(port);
            return this;
        }

        public Builder heartbeat(int heartbeat) {
            configuration.setHeartbeat(heartbeat);
            return this;
        }

        public Configuration build() {
            if (configuration.readTimeout < 5) {
                configuration.setReadTimeout(DEFAULT_READ_TIMEOUT);
            }
            if (configuration.getHost() == null || "".equals(configuration.getHost().trim())) {
                throw new IllegalArgumentException("Host can not be null or empty!");
            }
            if (configuration.getPort() <= 0 || configuration.getPort() > 65535) {
                throw new IllegalArgumentException("Invalid port !");
            }
            if (configuration.getHeartbeat() <= HEART_BEAT_INTERVAL) {
                configuration.setHeartbeat(HEART_BEAT_INTERVAL);
            }
            return configuration;
        }
    }
}
