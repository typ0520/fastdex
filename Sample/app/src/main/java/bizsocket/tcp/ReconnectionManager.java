package bizsocket.tcp;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;

/**
 * Handles the automatic reconnection process. Every time a connection is dropped without
 * the application explicitly closing it, the manager automatically tries to reconnect to
 * the server.
 */
public class ReconnectionManager {
    private Logger logger = LoggerFactory.getLogger(ReconnectionManager.class.getSimpleName());

    private int RANDOM_BASE = 5;

    private SocketConnection connection;
    private ReconnectionManager.ReconnectionThread reconnectionThread;
    private boolean done = false;
    private boolean needRecnect = false;
    private ReconnectHandler reconnectHandler;

    public void bind(SocketConnection connection) {
        this.connection = connection;
        this.connection.removeConnectionListener(connectionListener);
        this.connection = connection;
        this.connection.addConnectionListener(connectionListener);
    }

    public void unbind() {
        if (connection != null) {
            this.connection.removeConnectionListener(connectionListener);
        }

        this.connection = null;
    }

    public boolean isNeedRecnect() {
        return needRecnect;
    }

    public boolean isReconnectionAllowed() {
        return !this.done;
    }

    public void setDone(boolean done) {
        this.done = done;

        if (done) {
            if (connection != null) {
                connection.removeConnectionListener(connectionListener);
                connection = null;
            }
            if (reconnectionThread != null) {
                reconnectionThread.interrupt();
            }
        }
    }

    public ReconnectHandler getReconnectHandler() {
        return reconnectHandler;
    }

    public void setReconnectHandler(ReconnectHandler reconnectHandler) {
        this.reconnectHandler = reconnectHandler;
    }

    public synchronized void reconnect() {
        if(this.isReconnectionAllowed()) {
            if(this.reconnectionThread != null && this.reconnectionThread.isAlive()) {
                return;
            }

            this.reconnectionThread = new ReconnectionManager.ReconnectionThread();
            this.reconnectionThread.setName("Reconnection Manager");
            this.reconnectionThread.setDaemon(true);
            this.reconnectionThread.start();
        }
    }

    class ReconnectionThread extends Thread {
        private int attempts = 0;

        ReconnectionThread() {
        }

        public void resetAttempts() {
            this.attempts = 0;
        }

        private int timeDelay() {
            ++this.attempts;
            return this.attempts > 9 ? ReconnectionManager.this.RANDOM_BASE * 3
                    : ReconnectionManager.this.RANDOM_BASE;
        }

        public void run() {
            while(ReconnectionManager.this.isReconnectionAllowed() && !isInterrupted()) {
                int timeDelay = this.timeDelay();

                while(ReconnectionManager.this.isReconnectionAllowed() && timeDelay > 0 && !isInterrupted()) {
                    try {
                        Thread.sleep(1000L);
                        --timeDelay;
                        for (ConnectionListener listener : connection.connectionListeners) {
                            listener.reconnectingIn(timeDelay);
                        }
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }

                try {
                    if (isReconnectionAllowed() && !isInterrupted()) {
                        if (reconnectHandler != null) {
                            reconnectHandler.doReconnect(connection);
                        }
                        else {
                            logger.debug("connection.connect()  reconnect");
                            connection.reconnect();
                        }
                    }
                } catch (Exception exception) {
                    if (isReconnectionAllowed() && !isInterrupted()) {
                        connection.notifyConnectionError(exception);
                    }
                }

                logger.debug("reconnManager shutdown");
            }
        }
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    private ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void connected(SocketConnection connection) {
            logger.debug("SocketConnection connected");

            done = true;
            needRecnect = false;
            if (null != reconnectionThread) {
                reconnectionThread.resetAttempts();
            }
        }

        @Override
        public void connectionClosed() {
            logger.debug("SocketConnection connectionClosed");

            done = true;
            needRecnect = false;
            if (null != reconnectionThread) {
                reconnectionThread.resetAttempts();
            }
        }

        @Override
        public void connectionClosedOnError(Exception exception) {
            logger.debug("SocketConnection connectionClosedOnError");
            onConnectionClosedOnError(exception);
        }

        @Override
        public void reconnectingIn(int time) {
            logger.debug("SocketConnection reconnectingIn");

            logger.debug("reconnectingIn: " + time);
        }
    };

    public void onConnectionClosedOnError(Exception exception) {
        done = false;
        if (!isReconnectionAllowed()) {
            return;
        }
        needRecnect = true;
        reconnect();
    }

    public interface ReconnectHandler {
        void doReconnect(SocketConnection connection);
    }
}
