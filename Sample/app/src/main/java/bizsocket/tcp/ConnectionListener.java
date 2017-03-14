package bizsocket.tcp;

/**
 * Interface that allows for implementing classes to listen for connection closing
 * and reconnection events. Listeners are registered with SocketConnection objects.
 */
public interface ConnectionListener {
    /**
     * TODO
     * @param connection
     */
    void connected(SocketConnection connection);

    /**
     * Notification that the connection was closed normally or that the reconnection
     * process has been aborted.
     */
    void connectionClosed();

    /**
     * Notification that the connection was closed due to an exception. When
     * abruptly disconnected it is possible for the connection to try reconnecting
     * to the server.
     *
     * @param e the exception.
     */
    void connectionClosedOnError(Exception e);

    /**
     * The connection will retry to reconnect in the specified number of seconds.
     *
     * @param seconds remaining seconds before attempting a reconnection.
     */
    void reconnectingIn(int seconds);
}
