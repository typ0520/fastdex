package bizsocket.logger;

/**
 * Created by tong on 16/10/5.
 */
public class LoggerFactory {
    private static Class<? extends Logger> defaultLoggerType;

    public static void setDefaultLoggerType(Class<? extends Logger> defaultLoggerType) {
        LoggerFactory.defaultLoggerType = defaultLoggerType;
    }

    public static Class<? extends Logger> getDefaultLoggerType() {
        return defaultLoggerType == null ? SystemOutLogger.class : defaultLoggerType;
    }

    public static Logger getLogger(String tag) {
        try {
            if (tag == null) {
                tag = "BizSocket";
            }
            Logger logger = getDefaultLoggerType().getConstructor(String.class).newInstance(tag);
            return logger;
        } catch (Exception e) {

        }

        return new SystemOutLogger(tag);
    }
}
