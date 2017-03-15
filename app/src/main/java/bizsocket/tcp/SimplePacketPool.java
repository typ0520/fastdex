package bizsocket.tcp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tong on 16/10/19.
 */
public class SimplePacketPool implements PacketPool {
    private static final int QUEUE_SIZE = 60;
    private static final int COOLING_PERIOD = 10 * 1000;

    protected final Set<Entity> queue = Collections.synchronizedSet(new HashSet<Entity>());

    @Override
    public Packet pull() {
        synchronized (queue) {
            Entity entity = null;
            for (Entity e : queue) {
                if (e.isEnable()) {
                    entity = e;
                    break;
                }
            }
            if (entity != null) {
                queue.remove(entity);
                Packet packet = entity.packet;
                packet.setFlags(packet.getFlags() & ~Packet.FLAG_RECYCLED);
                packet.onPrepareReuse();
                return packet;
            }
        }
        return null;
    }

    @Override
    public void push(Packet packet) {
        if (queue.size() == QUEUE_SIZE) {
            return;
        }
        synchronized (queue) {
            queue.add(new Entity(packet));
        }
    }

    static class Entity {
        long saveMillis;
        Packet packet;

        public Entity(Packet packet) {
            this.packet = packet;
            saveMillis = System.currentTimeMillis();
        }

        boolean isEnable() {
            long current = System.currentTimeMillis();
            return current >= saveMillis + COOLING_PERIOD && (packet.getFlags() & Packet.FLAG_RECYCLED) != 0;
        }
    }
}
