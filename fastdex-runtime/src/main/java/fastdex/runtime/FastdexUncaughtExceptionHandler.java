package fastdex.runtime;

import android.content.Context;
import android.util.Log;

import fastdex.runtime.fd.Logging;

/**
 * Created by tong on 17/9/6.
 */
public class FastdexUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Context applicationContext;
    private final Thread.UncaughtExceptionHandler ueh;

    public FastdexUncaughtExceptionHandler(Context applicationContext) {
        ueh = Thread.getDefaultUncaughtExceptionHandler();
        this.applicationContext = applicationContext;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (applicationContext != null) {
            Fastdex fastdex = Fastdex.get();
            if (fastdex != null) {
                RuntimeMetaInfo runtimeMetaInfo = fastdex.readRuntimeMetaInfoFromFile();
                if (runtimeMetaInfo != null && (runtimeMetaInfo.getPatchDexVersion() > 0 || runtimeMetaInfo.getMergedDexVersion() > 0)) {
                    Throwable root = getRootThrowable(ex);
                    if (root instanceof IncompatibleClassChangeError) {
                        //重新触发安装
                        runtimeMetaInfo.setBuildMillis(-1);
                        Fastdex.get().saveRuntimeMetaInfo(runtimeMetaInfo);

                        String msg = "修改了抽象类导致的错误,Fastdex已经为你自动恢复状态，重新执行一次fastdex" + runtimeMetaInfo.getVariantName() + "任务就可以解决这个问题了";
                        ueh.uncaughtException(thread, new RuntimeException(msg,ex));
                        Log.e(Logging.LOG_TAG,msg);
                        return;
                    }
                }
            }
        }

        ueh.uncaughtException(thread, ex);
    }

    Throwable getRootThrowable(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        }
        if (cause == throwable) {
            return throwable;
        }
        return getRootThrowable(throwable.getCause());
    }
}
