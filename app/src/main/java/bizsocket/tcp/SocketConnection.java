package bizsocket.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Creates a socket connection to a tcp server.
 */
public abstract class SocketConnection implements Connection, ReconnectionManager.ReconnectHandler {
    public static final int DEFAULT_HEART_BEAT_INTERVAL = 30000;

    /**
     * A collection of ConnectionListeners which listen for connection closing
     * and reconnection events.
     */
    protected final Collection<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<ConnectionListener>();
    protected final Collection<PacketListener> packetListeners = new CopyOnWriteArrayList<PacketListener>();
    protected final PacketFactory packetFactory;
    protected final Logger logger = LoggerFactory.getLogger(SocketConnection.class.getSimpleName());

    private Socket socket;
    private String host;
    private int port;
    private BufferedSource reader;
    private BufferedSink writer;

    private PacketWriter packetWriter;
    private PacketReader packetReader;
    private Timer timer;
    private int heartbeat = DEFAULT_HEART_BEAT_INTERVAL;//心跳间隔
    private ReconnectionManager reconnectionManager;
    private Object lock = new Object();

    public SocketConnection() {
        this(null,0);
    }

    public SocketConnection(String host, int port) {
        this.host = host;
        this.port = port;

        packetFactory = createPacketFactory();
    }

    @Override
    public void connect() throws Exception {
        disconnect();

        logger.debug("connect host: " + host + " port: " + port);
        socket = createSocket(host,port);

        initConnection();

        onSocketConnected();
        callConnectionListenerConnected();
    }

    public boolean connectAndStartWatch() {
        logger.debug("connectAndStartWatch host: " + host + " port: " + port);
        try {
            bindReconnectionManager();
            connect();
        } catch (Exception e) {
            e.printStackTrace();

            notifyConnectException(e);
            return false;
        }

        return true;
    }

    @Override
    public void disconnect() {
        //if (socket != null && !isSocketClosed()) {
        if (socket != null) {
            logger.debug("disconnect");
            try {
                if (packetReader != null) {
                    packetReader.shutdown();
                }
            } catch (Throwable e) {
                //e.printStackTrace();
            }
            try {
                if (packetWriter != null) {
                    packetWriter.shutdown();
                }
            } catch (Throwable e) {
                //e.printStackTrace();
            }
            stopHeartBeat();
            try {
                socket.close();
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                socket.shutdownInput();
            } catch (Exception e) {
                //e.printStackTrace();
            }

            socket = null;

            for (ConnectionListener connectionListener : connectionListeners) {
                connectionListener.connectionClosed();
            }
        }
    }

    @Override
    public boolean isConnected() {
        return !isSocketClosed();
    }

    protected abstract PacketFactory createPacketFactory();

    public PacketFactory getPacketFactory() {
        return packetFactory;
    }

    public boolean isSocketClosed() {
        return socket == null || socket.isClosed() || !socket.isConnected();
    }

    public BufferedSource getReader() {
        return reader;
    }

    public BufferedSink getWriter() {
        return writer;
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
        if (connectionListeners.contains(connectionListener)) {
            return;
        }
        this.connectionListeners.add(connectionListener);
    }

    public void removeConnectionListener(ConnectionListener connectionListener) {
        this.connectionListeners.remove(connectionListener);
    }

    public void addPacketListener(PacketListener packetListener) {
        if (packetListeners.contains(packetListener)) {
            return;
        }
        packetListeners.add(packetListener);
    }

    public void removePacketListener(PacketListener packetListener) {
        packetListeners.remove(packetListener);
    }

    public void setHostAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public void reconnect() {
        try {
            connect();
        } catch (Exception e) {
            notifyConnectException(e);
        }
    }

    public void triggerReconnect() {
        bindReconnectionManager();

        notifyConnectException(new SocketException("触发重连"));
    }

    public void bindReconnectionManager() {
        if (reconnectionManager != null) {
            return;
        }

        reconnectionManager = new ReconnectionManager();
        reconnectionManager.bind(this);
        reconnectionManager.setReconnectHandler(this);
    }

    public void unbindReconnectionManager() {
        if (reconnectionManager != null) {
            reconnectionManager.unbind();
        }
        reconnectionManager = null;
    }

    protected Socket createSocket(String host, int port) throws Exception {
        Socket socket = new Socket(host, port);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        return socket;
    }

    private void notifyConnectException(Exception exception) {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connectionClosedOnError(exception);
        }
    }

    private void initReaderAndWriter() {
        try {
            reader = Okio.buffer(Okio.source(socket.getInputStream()));
            writer = Okio.buffer(Okio.sink(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initConnection() {
        if (isSocketClosed()) {
            return;
        }
        boolean isFirstInitialization = packetReader == null || packetWriter == null;

        initReaderAndWriter();
        if (isFirstInitialization) {
            this.packetWriter = new PacketWriter(this);
            this.packetReader = new PacketReader(this);
        }
        this.packetWriter.setWriter(writer);
        this.packetReader.setReader(reader);
        this.packetWriter.startup();
        this.packetReader.startup();
    }

    public void sendPacket(Packet packet) {
        if (isSocketClosed()) {
            return;
        }
        packetWriter.sendPacket(packet);
        packet.setPacketPool(getPacketFactory().getPacketPool());
    }

    public void startHeartBeat() {
        stopHeartBeat();
        synchronized (lock) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Packet packet = packetFactory.getHeartBeatPacket();
                    if (packet == null) {
                        return;
                    }
                    sendPacket(packet);
                }
            }, 0, heartbeat);
        }
    }

    private void stopHeartBeat() {
        synchronized (lock) {
            if (null != timer) {
                timer.cancel();
            }
            timer = null;
        }
    }

    public void handleReadWriteError(Exception e) {
        if ((e instanceof SocketException) || (e instanceof EOFException)) {
            notifyConnectionError(e);
        }
    }

    void notifyConnectionError(Exception exception) {
        stopHeartBeat();
        packetReader.shutdown();
        packetWriter.shutdown();

        // Notify connection listeners of the error.
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connectionClosedOnError(exception);
        }
    }

    private void callConnectionListenerConnected() {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connected(this);
        }
    }

    protected void onSocketConnected() {

    }

    /**
     * call this method when packet send successful
     * @param packet
     */
    void notifySendSuccessful(Packet packet) {
        for (PacketListener packetListener : packetListeners) {
            try {
                packetListener.onSendSuccessful(packet);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        if ((packet.getFlags() & Packet.FLAG_AUTO_RECYCLE_ON_SEND_SUCCESS) != 0) {
            packet.recycle();
        }
    }

    void handlerReceivedPacket(Packet packet) {
        packet.setPacketPool(getPacketFactory().getPacketPool());
        for (PacketListener packetListener : packetListeners) {
            try {
                packetListener.processPacket(packet);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void clearWriteQuete() {
        if (packetWriter != null) {
            packetWriter.clearQueue();
        }
    }

    @Override
    public void doReconnect(SocketConnection connection) {
        reconnect();
    }
}
