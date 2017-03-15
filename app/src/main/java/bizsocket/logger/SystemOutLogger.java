package bizsocket.logger;

/**
 * Created by tong on 16/10/5.
 */
public class SystemOutLogger extends Logger {
    public SystemOutLogger(String tag) {
        super(tag);
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void debug(String msg) {
        System.out.println(tag + ": " + msg);
    }

    @Override
    public void info(String msg) {
        System.out.println(tag + ": " + msg);
    }

    @Override
    public void warn(String msg) {
        System.out.println(tag + ": " + msg);
    }

    @Override
    public void error(String msg) {
        System.out.println(tag + ": " + msg);
    }
}
