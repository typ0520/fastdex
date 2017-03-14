package bizsocket.core;

import java.util.Arrays;

/**
 * 串行信息描述
 * Created by tong on 16/4/16.
 */
public class SerialSignal {
    /**
     * 必须等待的命令，需要是有序的
     */
    public static final int FLAG_ORDERED = 0x1;

    private int entranceCommand;//入口command
    private int[] strongReferences;//必须等待的命令
    private int[] weekReferences;//相关的命令不是必须等待的
    private int flags;
    private Class<? extends AbstractSerialContext> serialContextType;

    public int getEntranceCommand() {
        return entranceCommand;
    }

    public SerialSignal(Class<? extends AbstractSerialContext> serialContextType,int entranceCommand, int[] strongReferences) {
        this(serialContextType,entranceCommand,strongReferences,null);
    }

    public SerialSignal(Class<? extends AbstractSerialContext> serialContextType,int entranceCommand, int[] strongReferences, int[] weekReferences) {
        this.serialContextType = serialContextType;
        this.entranceCommand = entranceCommand;
        this.strongReferences = strongReferences;
        this.weekReferences = weekReferences;
    }

    public int[] getStrongReferences() {
        return strongReferences;
    }

    public int[] getWeekReferences() {
        return weekReferences;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public boolean isStrongReference(int command) {
        if (strongReferences != null) {
            for (int c : strongReferences) {
                if (command == c) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isWeekReference(int command) {
        if (weekReferences != null) {
            for (int c : weekReferences) {
                if (command == c) {
                    return true;
                }
            }
        }
        return false;
    }

    public Class<? extends AbstractSerialContext> getSerialContextType() {
        return serialContextType;
    }

    @Override
    public String toString() {
        return "SerialSignal{" +
                "entranceCommand=" + entranceCommand +
                ", strongReferences=" + Arrays.toString(strongReferences) +
                ", weekReferences=" + Arrays.toString(weekReferences) +
                '}';
    }
}

