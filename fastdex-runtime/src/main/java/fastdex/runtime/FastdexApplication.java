package fastdex.runtime;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import fastdex.runtime.loader.MultiDex;

/**
 * Created by tong on 17/3/11.
 */
public class FastdexApplication extends Application {
    public static final String LOG_TAG = "Fastdex";

    private Application realApplication;

    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(context);
        fixGoogleMultiDex();
        createRealApplication(this);
        invokeAttachBaseContext(context);
    }

    private void invokeAttachBaseContext(Context context) {
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

    public void onCreate() {
        super.onCreate();

        Fastdex.get().initialize(this,realApplication);
        if (this.realApplication != null) {
            this.realApplication.onCreate();
        }
    }

    private void fixGoogleMultiDex() {
        try {
            Class clazz = getClassLoader().loadClass("android.support.multidex.MultiDex");
            Field field = clazz.getDeclaredField("installedApk");
            field.setAccessible(true);
            Set<String> installedApk = (Set<String>) field.get(null);

            installedApk.addAll(MultiDex.installedApk);
        } catch (Throwable e) {

        }
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

    private String getOriginApplicationName(Context context) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appInfo.metaData.getString("FASTDEX_ORIGIN_APPLICATION_CLASSNAME");
    }


    public Context createPackageContext(String packageName, int flags)
            throws PackageManager.NameNotFoundException {
        Context c = this.realApplication.createPackageContext(packageName, flags);
        return c == null ? this.realApplication : c;
    }

    public void registerComponentCallbacks(ComponentCallbacks callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            this.realApplication.registerComponentCallbacks(callback);
        }
    }

    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            this.realApplication.registerActivityLifecycleCallbacks(callback);
        }
    }

    public void registerOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this.realApplication.registerOnProvideAssistDataListener(callback);
        }
    }

    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            this.realApplication.unregisterComponentCallbacks(callback);
        }
    }

    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            this.realApplication.unregisterActivityLifecycleCallbacks(callback);
        }
    }

    public void unregisterOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this.realApplication.unregisterOnProvideAssistDataListener(callback);
        }
    }
}

