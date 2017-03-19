package com.dx168.fastdex.runtime;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.dx168.fastdex.runtime.multidex.MultiDex;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        createRealApplication(context);

        if (this.realApplication != null)
            try {
                Method attachBaseContext = ContextWrapper.class
                        .getDeclaredMethod("attachBaseContext", new Class[]{Context.class});

                attachBaseContext.setAccessible(true);
                attachBaseContext.invoke(this.realApplication, new Object[]{context});
            } catch (Exception e) {
                throw new IllegalStateException(e);
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

    public static void monkeyPatchApplication(Context context, Application bootstrap, Application realApplication) {
        /*
        The code seems to perform this:
        Application realApplication = the newly instantiated (in attachBaseContext) user app
        currentActivityThread = ActivityThread.currentActivityThread;
        Application initialApplication = currentActivityThread.mInitialApplication;
        if (initialApplication == BootstrapApplication.this) {
            currentActivityThread.mInitialApplication = realApplication;
        // Replace all instance of the stub application in ActivityThread#mAllApplications with the
        // real one
        List<Application> allApplications = currentActivityThread.mAllApplications;
        for (int i = 0; i < allApplications.size(); i++) {
            if (allApplications.get(i) == BootstrapApplication.this) {
                allApplications.set(i, realApplication);
            }
        }
        // Enumerate all LoadedApk (or PackageInfo) fields in ActivityThread#mPackages and
        // ActivityThread#mResourcePackages and do two things:
        //   - Replace the Application instance in its mApplication field with the real one
        //   - Replace mResDir to point to the external resource file instead of the .apk. This is
        //     used as the asset path for new Resources objects.
        //   - Set Application#mLoadedApk to the found LoadedApk instance
        ArrayMap<String, WeakReference<LoadedApk>> map1 = currentActivityThread.mPackages;
        for (Map.Entry<String, WeakReference<?>> entry : map1.entrySet()) {
            Object loadedApk = entry.getValue().get();
            if (loadedApk == null) {
                continue;
            }
            if (loadedApk.mApplication == BootstrapApplication.this) {
                loadedApk.mApplication = realApplication;
                if (externalResourceFile != null) {
                    loadedApk.mResDir = externalResourceFile;
                }
                realApplication.mLoadedApk = loadedApk;
            }
        }
        // Exactly the same as above, except done for mResourcePackages instead of mPackages
        ArrayMap<String, WeakReference<LoadedApk>> map2 = currentActivityThread.mResourcePackages;
        for (Map.Entry<String, WeakReference<?>> entry : map2.entrySet()) {
            Object loadedApk = entry.getValue().get();
            if (loadedApk == null) {
                continue;
            }
            if (loadedApk.mApplication == BootstrapApplication.this) {
                loadedApk.mApplication = realApplication;
                if (externalResourceFile != null) {
                    loadedApk.mResDir = externalResourceFile;
                }
                realApplication.mLoadedApk = loadedApk;
            }
        }
        */
        // BootstrapApplication is created by reflection in Application#handleBindApplication() ->
        // LoadedApk#makeApplication(), and its return value is used to set the Application field in all
        // sorts of Android internals.
        //
        // Fortunately, Application#onCreate() is called quite soon after, so what we do is monkey
        // patch in the real Application instance in BootstrapApplication#onCreate().
        //
        // A few places directly use the created Application instance (as opposed to the fields it is
        // eventually stored in). Fortunately, it's easy to forward those to the actual real
        // Application class.
        try {
            // Find the ActivityThread instance for the current thread
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object currentActivityThread = getActivityThread(context, activityThread);
            // Find the mInitialApplication field of the ActivityThread to the real application
            Field mInitialApplication = activityThread.getDeclaredField("mInitialApplication");
            mInitialApplication.setAccessible(true);
            Application initialApplication = (Application) mInitialApplication.get(currentActivityThread);
            if (realApplication != null && initialApplication == bootstrap) {
                mInitialApplication.set(currentActivityThread, realApplication);
            }
            // Replace all instance of the stub application in ActivityThread#mAllApplications with the
            // real one
            if (realApplication != null) {
                Field mAllApplications = activityThread.getDeclaredField("mAllApplications");
                mAllApplications.setAccessible(true);
                List<Application> allApplications = (List<Application>) mAllApplications
                        .get(currentActivityThread);
                for (int i = 0; i < allApplications.size(); i++) {
                    if (allApplications.get(i) == bootstrap) {
                        allApplications.set(i, realApplication);
                    }
                }
            }
            // Figure out how loaded APKs are stored.
            // API version 8 has PackageInfo, 10 has LoadedApk. 9, I don't know.
            Class<?> loadedApkClass;
            try {
                loadedApkClass = Class.forName("android.app.LoadedApk");
            } catch (ClassNotFoundException e) {
                loadedApkClass = Class.forName("android.app.ActivityThread$PackageInfo");
            }
            Field mApplication = loadedApkClass.getDeclaredField("mApplication");
            mApplication.setAccessible(true);
            Field mResDir = loadedApkClass.getDeclaredField("mResDir");
            mResDir.setAccessible(true);
            // 10 doesn't have this field, 14 does. Fortunately, there are not many Honeycomb devices
            // floating around.
            Field mLoadedApk = null;
            try {
                mLoadedApk = Application.class.getDeclaredField("mLoadedApk");
            } catch (NoSuchFieldException e) {
                // According to testing, it's okay to ignore this.
            }
            // Enumerate all LoadedApk (or PackageInfo) fields in ActivityThread#mPackages and
            // ActivityThread#mResourcePackages and do two things:
            //   - Replace the Application instance in its mApplication field with the real one
            //   - Replace mResDir to point to the external resource file instead of the .apk. This is
            //     used as the asset path for new Resources objects.
            //   - Set Application#mLoadedApk to the found LoadedApk instance
            for (String fieldName : new String[]{"mPackages", "mResourcePackages"}) {
                Field field = activityThread.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(currentActivityThread);
                for (Map.Entry<String, WeakReference<?>> entry :
                        ((Map<String, WeakReference<?>>) value).entrySet()) {
                    Object loadedApk = entry.getValue().get();
                    if (loadedApk == null) {
                        continue;
                    }
                    if (mApplication.get(loadedApk) == bootstrap) {
                        if (realApplication != null) {
                            mApplication.set(loadedApk, realApplication);
                        }
//                        if (externalResourceFile != null) {
//                            mResDir.set(loadedApk, externalResourceFile);
//                        }
                        if (realApplication != null && mLoadedApk != null) {
                            mLoadedApk.set(realApplication, loadedApk);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public static Object getActivityThread( Context context,
                                            Class<?> activityThread) {
        try {
            if (activityThread == null) {
                activityThread = Class.forName("android.app.ActivityThread");
            }
            Method m = activityThread.getMethod("currentActivityThread");
            m.setAccessible(true);
            Object currentActivityThread = m.invoke(null);
            if (currentActivityThread == null && context != null) {
                // In older versions of Android (prior to frameworks/base 66a017b63461a22842)
                // the currentActivityThread was built on thread locals, so we'll need to try
                // even harder
                Field mLoadedApk = context.getClass().getField("mLoadedApk");
                mLoadedApk.setAccessible(true);
                Object apk = mLoadedApk.get(context);
                Field mActivityThreadField = apk.getClass().getDeclaredField("mActivityThread");
                mActivityThreadField.setAccessible(true);
                currentActivityThread = mActivityThreadField.get(apk);
            }
            return currentActivityThread;
        } catch (Throwable ignore) {
            return null;
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

        if (this.realApplication != null) {
            this.realApplication.onCreate();

            monkeyPatchApplication(this,this,realApplication);
        }
    }
}

