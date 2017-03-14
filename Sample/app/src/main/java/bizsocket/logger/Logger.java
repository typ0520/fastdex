package bizsocket.logger;

/**
 * Created by tong on 16/10/5.
 */
public abstract class Logger {
    public String tag;

    public Logger(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public abstract boolean isEnable();

    public abstract void debug(String msg);

    public abstract void info(String msg);

    public abstract void warn(String msg);

    public abstract void error(String msg);
}
