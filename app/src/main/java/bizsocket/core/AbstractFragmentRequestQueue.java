package bizsocket.core;

import bizsocket.tcp.Packet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持包分段的队列
 * Created by tong on 16/3/8.
 */
public abstract class AbstractFragmentRequestQueue<T extends Packet> extends RequestQueue {
    private final Map<Integer,FragmentInfo<T>> fragmentInfoMap = new ConcurrentHashMap<Integer,FragmentInfo<T>>();

    public AbstractFragmentRequestQueue(AbstractBizSocket bizSocket) {
        super(bizSocket);
    }

    /**
     * 合并片段
     * @param fragmentInfo
     * @return
     */
    public abstract T mergeFragment(FragmentInfo fragmentInfo);

    /**
     * 获取分段总数量
     * @param packet
     * @return 1 不是分段的包
     */
    public abstract int getFragmentCount(T packet);

    /**
     * 是否是分段的包
     * @param packet
     * @return
     */
    public abstract boolean isFragment(T packet);

    /**
     * 获取包的序列号
     * @param packet
     * @return
     */
    public abstract int getSequence(T packet);

    /**
     * 有包分片时，分片序号(索引从0开始)
     * @param packet
     * @return
     */
    public abstract int getFragmentIndex(T packet);

    @Override
    public void processPacket(Packet packet) {
        if (isFragment((T) packet)) {
            int sequence = getSequence((T) packet);
            FragmentInfo fragmentInfo = getFragmentInfoBySeq(sequence);
            if (fragmentInfo == null) {
                fragmentInfo = new FragmentInfo(sequence,getFragmentCount((T) packet));
                putFragmentInfo(sequence,fragmentInfo);
            }
            int index = getFragmentIndex((T) packet);
            fragmentInfo.putFragment(index,packet);

            if (isComplete(fragmentInfo)) {
                Packet targetPacket = mergeFragment(fragmentInfo);
                if (targetPacket != null) {
                    super.processPacket(targetPacket);

                    removeFragmentInfo(sequence,fragmentInfo);
                }
            }
        } else {
            super.processPacket(packet);
        }
    }

    private void removeFragmentInfo(int sequence, FragmentInfo fragmentInfo) {
        fragmentInfoMap.remove(sequence);
    }

    /**
     * 通过包的业务序列号获取分段信息
     * @param sequence
     * @return
     */
    public FragmentInfo getFragmentInfoBySeq(int sequence) {
        return fragmentInfoMap.get(sequence);
    }

    /**
     * 保存业务序列号与分段信息的映射
     * @param sequence
     * @param fragmentInfo
     */
    public void putFragmentInfo(int sequence, FragmentInfo fragmentInfo) {
        fragmentInfoMap.put(sequence,fragmentInfo);
    }

    /**
     * 是否接收完数据
     * @param fragmentInfo
     * @return
     */
    public boolean isComplete(FragmentInfo fragmentInfo) {
        return fragmentInfo.getPackets() != null
                && fragmentInfo.getPackets().size() == fragmentInfo.getTotalSize();
    }

    /**
     * 清空分片的包
     */
    public void clearFragment() {
        fragmentInfoMap.clear();
    }

    @Override
    public void connectionClosed() {
        clearFragment();
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        clearFragment();
    }

    /**
     * 分段数据
     * @param <T>
     */
    public static class FragmentInfo<T extends Packet> {
        private int sequence;//包的序列号,业务使用
        private int totalSize;//有包分片时，分片总数
        private List<T> packets;

        public FragmentInfo(int sequence, int totalSize) {
            this.sequence = sequence;
            this.totalSize = totalSize;
        }

        public int getSequence() {
            return sequence;
        }

        public void setSequence(int sequence) {
            this.sequence = sequence;
        }

        public int getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(int totalSize) {
            this.totalSize = totalSize;
        }

        public List<T> getPackets() {
            return packets;
        }

        public void setPackets(List<T> packets) {
            this.packets = packets;
        }

        public void putFragment(int index,T packet) {
            if (packets == null) {
                packets = new ArrayList<T>(totalSize);
            }
            if (index >= totalSize) {
                throw new IllegalStateException("Invalid fragment index");
            }
            packets.add(index,packet);
        }
    }
}
