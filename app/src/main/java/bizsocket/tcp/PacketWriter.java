package bizsocket.tcp;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import bizsocket.logger.Logger;
import bizsocket.logger.LoggerFactory;
import okio.BufferedSink;

/**
 * Writes packets to a tcp server. Packets are sent using a dedicated thread. Packet
 * interceptors can be registered to dynamically modify packets before they're actually
 * sent. Packet listeners can be registered to listen for all outgoing packets.
 */
class PacketWriter {
    private final SocketConnection connection;
    private Thread writerThread;
    private BufferedSink writer;
    private final BlockingQueue<Packet> queue = new ArrayBlockingQueue<Packet>(500, true);
    private volatile boolean done = false;
    private final Logger logger = LoggerFactory.getLogger(PacketWriter.class.getSimpleName());

    public PacketWriter(SocketConnection connection) {
        this.connection = connection;
        this.init();
    }

    /**
     * Initializes the reader in order to be used. The reader is initialized during the
     * first connection and when reconnecting due to an abruptly disconnection.
     *
     */
    protected void init() {
        this.writer = connection.getWriter();
        done = false;
    }

    /**
     * Sends the specified packet to the server.
     *
     * @param packet the packet to send.
     */
    public void sendPacket(Packet packet) {
        if (this.done) {
            return;
        }

        try {
            queue.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (queue) {
            queue.notifyAll();
        }
    }

    /**
     * Starts the packet reader thread and returns once a connection to the server
     * has been established or if the server's features could not be parsed within
     * the connection's PacketReplyTimeout.
     */
    public synchronized void startup() {
        done = false;
        if (writerThread != null && writerThread.isAlive()) {
            return;
        }

        logger.debug("writer thread startup");
        writerThread = new Thread() {
            public void run() {
                PacketWriter.this.writePackets(this);
            }
        };
        writerThread.setName("Packet Writer");
        writerThread.setDaemon(true);
        writerThread.start();
    }

    void setWriter(BufferedSink writer) {
        this.writer = writer;
    }

    /**
     * Shuts the packet reader down. This method simply sets the 'done' flag to true.
     */
    public void shutdown() {
        if (this.done) {
            return;
        }

        logger.debug("writer thread shutdown");
        this.done = true;

        synchronized (queue) {
            queue.notifyAll();
        }

        if (writerThread != null) {
            writerThread.interrupt();
            writerThread = null;
        }
    }

    /**
     * Returns the next available packet from the queue for writing.
     *
     * @return the next packet for writing.
     */
    private Packet nextPacket() {
        Packet packet = null;
        while (!this.done && (packet = queue.poll()) == null) {
            try {
                synchronized (queue) {
                    queue.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return packet;
    }

    private void writePackets(Thread thisThread) {
        while (!this.done && this.writerThread == thisThread) {
            Packet packet = nextPacket();
            if (packet != null && !done && this.writerThread == thisThread) {
                try {
                    byte[] st = packet.toBytes();
                    writer.write(st);
                    writer.flush();

                    connection.notifySendSuccessful(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (!done && this.writerThread == thisThread) {
                        connection.handleReadWriteError(e);
                    }
                }
            }

//            try {
//                if (!done) {
//                    Thread.sleep(500);
//                }
//            } catch (InterruptedException e) {
//
//            }
        }
    }

    public void clearQueue() {
        if (queue != null) {
            queue.clear();
        }
    }
}
