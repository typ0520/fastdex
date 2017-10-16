/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fastdex.runtime.fd;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import fastdex.common.utils.FileUtils;
import fastdex.runtime.Constants;
import fastdex.runtime.MiddlewareActivity;
import fastdex.runtime.Fastdex;
import fastdex.runtime.RuntimeMetaInfo;
import fastdex.runtime.loader.MonkeyPatcher;

import static fastdex.common.fd.ProtocolConstants.UPDATE_MODE_COLD_SWAP;
import static fastdex.common.fd.ProtocolConstants.UPDATE_MODE_WARM_SWAP;

/**
 * Handler capable of restarting parts of the application in order for changes to become
 * apparent to the user:
 * <ul>
 *     <li> Apply a tiny change immediately - possible if we can detect that the change
 *          is only used in a limited context (such as in a layout) and we can directly
 *          poke the view hierarchy and schedule a paint.
 *     <li> Apply a change to the current activity. We can restart just the activity
 *          while the app continues running.
 *     <li> Restart the app with state persistence (simulates what happens when a user
 *          puts an app in the background, then it gets killed by the memory monitor,
 *          and then restored when the user brings it back
 *     <li> Restart the app completely.
 * </ul>
 */
public class Restarter {
    public static final int ACTIVITY_NONE = 0;
    public static final int ACTIVITY_CREATED = 1;
    public static final int ACTIVITY_STARTED = 2;
    public static final int ACTIVITY_RESUMED = 3;

    private static final WeakHashMap<Activity, Integer> sActivitiesRefs = new WeakHashMap();

    private static long sFirstTaskId = 0L;

    private static final Application.ActivityLifecycleCallbacks sLifecycleCallback = new Application.ActivityLifecycleCallbacks() {

        public void onActivityStopped(Activity activity) {
            sActivitiesRefs.put(activity, ACTIVITY_CREATED);
        }

        public void onActivityStarted(Activity activity) {
            sActivitiesRefs.put(activity, ACTIVITY_STARTED);
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        public void onActivityResumed(Activity activity) {
            sActivitiesRefs.put(activity, ACTIVITY_RESUMED);
            RuntimeMetaInfo runtimeMetaInfo = Fastdex.get().readRuntimeMetaInfoFromFile(false);
            if (runtimeMetaInfo != null && !runtimeMetaInfo.isActive()) {
                runtimeMetaInfo.setActive(true);
                Fastdex.get().saveRuntimeMetaInfo(runtimeMetaInfo,false);
            }
        }

        public void onActivityPaused(Activity activity) {
            sActivitiesRefs.put(activity, ACTIVITY_STARTED);
            RuntimeMetaInfo runtimeMetaInfo = Fastdex.get().readRuntimeMetaInfoFromFile();
            if (runtimeMetaInfo != null) {
                boolean active = getTopActivity() != null;

                if (runtimeMetaInfo.isActive() != active) {
                    runtimeMetaInfo.setActive(active);
                    Fastdex.get().saveRuntimeMetaInfo(runtimeMetaInfo,true);
                }
            }
        }

        public void onActivityDestroyed(Activity activity) {
            sActivitiesRefs.remove(activity);
        }

        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            sActivitiesRefs.put(activity, ACTIVITY_CREATED);

            if (sFirstTaskId == 0L) {
                sFirstTaskId = activity.getTaskId();
            }
        }
    };

    public static void initialize(Application app) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            app.registerActivityLifecycleCallbacks(sLifecycleCallback);
        }
    }

    /**
     * 重启app
     * @param updateMode
     * @param hasDex
     * @param hasResources
     * @param preparedPatchPath
     * @param resourcesApkPath
     * @param toast
     * @param restartAppByCmd
     */
    public static void restart(int updateMode, boolean hasDex, boolean hasResources, String preparedPatchPath, String resourcesApkPath, boolean toast, boolean restartAppByCmd) {
        Log.v(Logging.LOG_TAG, "Finished loading changes; update mode =" + updateMode);
        Log.v(Logging.LOG_TAG, "Restart app hasDex: " + hasDex + " ,hasResources: " + hasResources + ", resourcesApkPath: " + resourcesApkPath + ", preparedPatchPath: " + preparedPatchPath);
        RuntimeMetaInfo runtimeMetaInfo = Fastdex.get().readRuntimeMetaInfoFromFile();

        final Context context = Fastdex.get().getApplicationContext();
        Activity activity = getTopActivity();

        if (!hasDex && hasResources && updateMode == UPDATE_MODE_WARM_SWAP) {
            final List<Activity> activities = Restarter.getAllActivities();

            // Try to just replace the resources on the fly!
            File resDir = new File(runtimeMetaInfo.getPreparedPatchPath(), Constants.RES_DIR);
            File file = new File(resDir, resourcesApkPath);
            Log.v(Logging.LOG_TAG, "About to update resource file=" + file + ", activities=" + activities);

            if (FileUtils.isLegalFile(file)) {
                String resources = file.getAbsolutePath();
                MonkeyPatcher.monkeyPatchExistingResources(context, resources, activities);
            } else {
                Log.e(Logging.LOG_TAG, "No resource file found to apply");
                updateMode = UPDATE_MODE_COLD_SWAP;
            }
        }
        if (updateMode == UPDATE_MODE_WARM_SWAP) {
            if (activity != null) {
                if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                    Log.v(Logging.LOG_TAG, "Restarting activity only!");
                }

                if (toast) {
                    Restarter.showToast(activity, "Applied changes, restarted activity");
                }
                Restarter.restartActivityOnUiThread(activity);
                return;
            }

            if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                Log.v(Logging.LOG_TAG, "No activity found, falling through to do a full app restart");
            }
            updateMode = UPDATE_MODE_COLD_SWAP;
        }

        if (!restartAppByCmd && updateMode == UPDATE_MODE_COLD_SWAP) {
            //restart app
            if (activity != null && (activity instanceof MiddlewareActivity)) {
                ((MiddlewareActivity)activity).reset();
            } else {
                try {
                    Intent e = new Intent(context, MiddlewareActivity.class);
                    e.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    e.putExtra("reset", true);
                    context.startActivity(e);
                } catch (Exception exception) {
                    final String str = "Fail to increment build, make sure you have <Activity android:name=\"" + MiddlewareActivity.class.getName() + "\"/> registered in AndroidManifest.xml";

                    if (Log.isLoggable(Logging.LOG_TAG, Log.ERROR)) {
                        Log.e(Logging.LOG_TAG, str);
                    }

                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, str, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }
    }

    static List<Activity> getAllActivities() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ArrayList<Activity> list = new ArrayList<>();
            for (Map.Entry<Activity, Integer> e : sActivitiesRefs.entrySet()) {
                Activity a = e.getKey();
                if (a != null && e.getValue().intValue() > 0) {
                    list.add(a);
                }
            }
            return list;
        }
        else {
            return getActivities(Fastdex.get().getApplicationContext(),true);
        }
    }

    static Activity getTopActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Activity r = null;
            for (Map.Entry<Activity, Integer> e : sActivitiesRefs.entrySet()) {
                Activity a = e.getKey();
                if (a != null && e.getValue().intValue() == ACTIVITY_RESUMED) {
                    r = a;
                }
            }
            return r;
        }
        else {
            return getForegroundActivity(Fastdex.get().getApplicationContext());
        }
    }

    /** Restart an activity. Should preserve as much state as possible. */
    static void restartActivityOnUiThread(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                    Log.v(Logging.LOG_TAG, "Resources updated: notify activities");
                }
                updateActivity(activity);
            }
        });
    }

    static Activity getForegroundActivity(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return getTopActivity();
        }
        List<Activity> list = getActivities(context, true);
        return list.isEmpty() ? null : list.get(0);
    }

    // http://stackoverflow.com/questions/11411395/how-to-get-current-foreground-activity-context-in-android
    static List<Activity> getActivities(Context context, boolean foregroundOnly) {
        List<Activity> list = new ArrayList<Activity>();
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = MonkeyPatcher.getActivityThread(context, activityThreadClass);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Collection c;
            Object collection = activitiesField.get(activityThread);

            if (collection instanceof HashMap) {
                // Older platforms
                Map activities = (HashMap) collection;
                c = activities.values();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    collection instanceof ArrayMap) {
                ArrayMap activities = (ArrayMap) collection;
                c = activities.values();
            } else {
                return list;
            }
            for (Object activityRecord : c) {
                Class activityRecordClass = activityRecord.getClass();
                if (foregroundOnly) {
                    Field pausedField = activityRecordClass.getDeclaredField("paused");
                    pausedField.setAccessible(true);
                    if (pausedField.getBoolean(activityRecord)) {
                        continue;
                    }
                }
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                if (activity != null) {
                    list.add(activity);
                }
            }
        } catch (Throwable ignore) {
        }
        return list;
    }

    static void updateActivity(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                Log.v(Logging.LOG_TAG, "About to restart " + activity.getClass().getSimpleName());
            }

            // You can't restart activities that have parents: find the top-most activity
            while (activity.getParent() != null) {
                if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                    Log.v(Logging.LOG_TAG, activity.getClass().getSimpleName()
                            + " is not a top level activity; restarting "
                            + activity.getParent().getClass().getSimpleName() + " instead");
                }
                activity = activity.getParent();
            }

            // Directly supported by the framework!
            activity.recreate();
        }
    }

    static void showToast(final Activity activity, final String text) {
        if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
            Log.v(Logging.LOG_TAG, "About to show toast for activity " + activity + ": " + text);
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = activity.getApplicationContext();
                    if (context instanceof ContextWrapper) {
                        Context base = ((ContextWrapper) context).getBaseContext();
                        if (base == null) {
                            if (Log.isLoggable(Logging.LOG_TAG, Log.WARN)) {
                                Log.w(Logging.LOG_TAG, "Couldn't show toast: no base context");
                            }
                            return;
                        }
                    }

                    // For longer messages, leave the message up longer
                    int duration = Toast.LENGTH_SHORT;
                    if (text.length() >= 60 || text.indexOf('\n') != -1) {
                        duration = Toast.LENGTH_LONG;
                    }

                    // Avoid crashing when not available, e.g.
                    //   java.lang.RuntimeException: Can't create handler inside thread that has
                    //        not called Looper.prepare()
                    Toast.makeText(activity, text, duration).show();
                } catch (Throwable e) {
                    if (Log.isLoggable(Logging.LOG_TAG, Log.WARN)) {
                        Log.w(Logging.LOG_TAG, "Couldn't show toast", e);
                    }
                }
            }
        });
    }

    public static void showToast(String text) {
        Activity foreground = getTopActivity();
        if (foreground != null) {
            Restarter.showToast(foreground, text);
        } else if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
            Log.v(Logging.LOG_TAG, "Couldn't show toast (no activity) : " + text);
        }
    }
}
