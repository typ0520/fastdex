package fastdex.runtime;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import fastdex.runtime.fastdex.Fastdex;
import fastdex.runtime.fd.AppInfo;
import fastdex.runtime.fd.Logging;
import fastdex.runtime.fd.MonkeyPatcher;
import fastdex.runtime.fd.Server;
import fastdex.runtime.multidex.MultiDex;

/**
 * Created by tong on 17/3/11.
 */
public class FastdexApplication extends Application {
    public static final String LOG_TAG = "Fastdex";
    private Application realApplication;

    private String getOriginApplicationName(Context context) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String msg = appInfo.metaData.getString("FASTDEX_ORIGIN_APPLICATION_CLASSNAME");
        return msg;
    }

    private void createRealApplication(Context context) {
       String applicationClass = getOriginApplicationName(context);
        if (applicationClass != null) {
            Log.d(LOG_TAG, new StringBuilder().append("About to create real application of class name = ").append(applicationClass).toString());

            try {
                Class realClass = Class.forName(applicationClass);
                Constructor constructor = realClass.getConstructor(new Class[0]);
                this.realApplication = ((Application) constructor.newInstance(new Object[0]));
                Log.v(LOG_TAG, new StringBuilder().append("Created real app instance successfully :").append(this.realApplication).toString());

            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            this.realApplication = new Application();
        }
    }

    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(context);
        fixGoogleMultiDex(context);
        Fastdex.get(context).onAttachBaseContext(this);
        createRealApplication(context);

        if (this.realApplication != null) {
            try {
                Method attachBaseContext = ContextWrapper.class
                        .getDeclaredMethod("attachBaseContext", new Class[]{Context.class});

                attachBaseContext.setAccessible(true);
                attachBaseContext.invoke(this.realApplication, new Object[]{context});
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void fixGoogleMultiDex(Context context) {
        try {
            Class clazz = getClassLoader().loadClass("android.support.multidex.MultiDex");
            Field field = clazz.getDeclaredField("installedApk");
            field.setAccessible(true);
            Set<String> installedApk = (Set<String>) field.get(null);

            installedApk.addAll(MultiDex.installedApk);
        } catch (Throwable e) {

        }
    }

    public Context createPackageContext(String packageName, int flags)
            throws PackageManager.NameNotFoundException {
        Context c = this.realApplication.createPackageContext(packageName, flags);
        return c == null ? this.realApplication : c;
    }

    public void registerComponentCallbacks(ComponentCallbacks callback) {
        this.realApplication.registerComponentCallbacks(callback);
    }

    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        this.realApplication.registerActivityLifecycleCallbacks(callback);
    }

    public void registerOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this.realApplication.registerOnProvideAssistDataListener(callback);
        }
    }

    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        this.realApplication.unregisterComponentCallbacks(callback);
    }

    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        this.realApplication.unregisterActivityLifecycleCallbacks(callback);
    }

    public void unregisterOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this.realApplication.unregisterOnProvideAssistDataListener(callback);
        }
    }

    public void onCreate() {
        super.onCreate();

        if (Fastdex.get(this).isFastdexEnabled()) {
            startServer();
        }
        if (this.realApplication != null) {
            MonkeyPatcher.monkeyPatchApplication(this,this,realApplication);
            this.realApplication.onCreate();
        }
    }

    private void startServer() {
        Log.d(Logging.LOG_TAG, "Starting Instant Run Server for " + getPackageName());

        // Start server, unless we're in a multi-process scenario and this isn't the
        // primary process
        try {
            boolean foundPackage = false;
            int pid = Process.myPid();
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();

            boolean startServer;
            if (processes != null && processes.size() > 1) {
                // Multiple processes: look at each, and if the process name matches
                // the package name (for the current pid), it's the main process.
                startServer = false;
                for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
                    if (AppInfo.applicationId.equals(processInfo.processName)) {
                        foundPackage = true;
                        if (processInfo.pid == pid) {
                            startServer = true;
                            break;
                        }
                    }
                }
                if (!startServer && !foundPackage) {
                    // Safety check: If for some reason we didn't even find the main package,
                    // start the server anyway. This safeguards against apps doing strange
                    // things with the process name.
                    startServer = true;
                    Log.d(Logging.LOG_TAG, "Multiprocess but didn't find process with package: "
                            + "starting server anyway");
                }
            } else {
                // If there is only one process, start the server.
                startServer = true;
            }

            if (startServer) {
                Server.create(this);
            } else {
                Log.d(Logging.LOG_TAG, "In secondary process: Not starting server");

            }
        } catch (Throwable t) {
            Log.d(Logging.LOG_TAG, "Failed during multi process check", t);
            Server.create(this);
        }
    }
}

