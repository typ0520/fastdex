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

import static fastdex.common.fd.ProtocolConstants.MESSAGE_EOF;
import static fastdex.common.fd.ProtocolConstants.MESSAGE_PATCHES;
import static fastdex.common.fd.ProtocolConstants.MESSAGE_PATH_CHECKSUM;
import static fastdex.common.fd.ProtocolConstants.MESSAGE_PATH_EXISTS;
import static fastdex.common.fd.ProtocolConstants.MESSAGE_PING;
import static fastdex.common.fd.ProtocolConstants.MESSAGE_PING_AND_SHOW_TOAST;
import static fastdex.common.fd.ProtocolConstants.MESSAGE_RESTART_ACTIVITY;
import static fastdex.common.fd.ProtocolConstants.MESSAGE_SHOW_TOAST;
import static fastdex.common.fd.ProtocolConstants.PROTOCOL_IDENTIFIER;
import static fastdex.common.fd.ProtocolConstants.PROTOCOL_VERSION;
import static fastdex.common.fd.ProtocolConstants.UPDATE_MODE_COLD_SWAP;
import static fastdex.common.fd.ProtocolConstants.UPDATE_MODE_HOT_SWAP;
import static fastdex.common.fd.ProtocolConstants.UPDATE_MODE_NONE;
import static fastdex.common.fd.ProtocolConstants.UPDATE_MODE_WARM_SWAP;

import android.app.Activity;
import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import fastdex.common.ShareConstants;
import fastdex.common.utils.FileUtils;
import fastdex.runtime.Constants;
import fastdex.runtime.fastdex.Fastdex;
import fastdex.runtime.fastdex.RuntimeMetaInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Server running in the app listening for messages from the IDE and updating the code and resources
 * when provided
 */
public class Server {

    /**
     * Temporary debugging: have the server emit a message to the log every 30 seconds to
     * indicate whether it's still alive
     */
    private static final boolean POST_ALIVE_STATUS = false;

    private LocalServerSocket serverSocket;

    private final Context context;

    private final Handler handler = new Handler();

    private static int wrongTokenCount;


    public static Server create(Context context) {
        return new Server(context.getPackageName(), context);
    }

    private Server(String packageName, Context context) {
        this.context = context;
        try {
            serverSocket = new LocalServerSocket(packageName);
            if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                Log.v(Logging.LOG_TAG, "Starting server socket listening for package " + packageName
                        + " on " + serverSocket.getLocalSocketAddress());
            }
        } catch (IOException e) {
            Log.e(Logging.LOG_TAG, "IO Error creating local socket at " + packageName, e);
            return;
        }
        startServer();

        if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
            Log.v(Logging.LOG_TAG, "Started server for package " + packageName);
        }
    }

    private void startServer() {
        try {
            Thread socketServerThread = new Thread(new SocketServerThread());
            socketServerThread.start();
        } catch (Throwable e) {
            // Make sure an exception doesn't cause the rest of the user's
            // onCreate() method to be invoked
            if (Log.isLoggable(Logging.LOG_TAG, Log.ERROR)) {
                Log.e(Logging.LOG_TAG, "Fatal error starting Instant Run server", e);
            }
        }
    }

    public void shutdown() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignore) {
            }
            serverSocket = null;
        }
    }

    private class SocketServerThread extends Thread {
        @Override
        public void run() {
            if (POST_ALIVE_STATUS) {
                final Handler handler = new Handler();
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.v(Logging.LOG_TAG, "Instant Run server still here...");
                            }
                        });
                    }
                };

                timer.schedule(task, 1, 30000L);
            }

            while (true) {
                try {
                    LocalServerSocket serverSocket = Server.this.serverSocket;
                    if (serverSocket == null) {
                        break; // stopped?
                    }
                    LocalSocket socket = serverSocket.accept();

                    if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                        Log.v(Logging.LOG_TAG, "Received connection from IDE: spawning connection thread");
                    }

                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(socket);
                    socketServerReplyThread.run();

                    if (wrongTokenCount > 50) {
                        if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                            Log.v(Logging.LOG_TAG, "Stopping server: too many wrong token connections");
                        }
                        Server.this.serverSocket.close();
                        break;
                    }
                } catch (Throwable e) {
                    if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                        Log.v(Logging.LOG_TAG, "Fatal error accepting connection on local socket", e);
                    }
                }
            }
        }
    }

    private class SocketServerReplyThread extends Thread {

        private final LocalSocket socket;

        SocketServerReplyThread(LocalSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                try {
                    handle(input, output);
                } finally {
                    try {
                        input.close();
                    } catch (IOException ignore) {
                    }
                    try {
                        output.close();
                    } catch (IOException ignore) {
                    }
                }
            } catch (IOException e) {
                if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                    Log.v(Logging.LOG_TAG, "Fatal error receiving messages", e);
                }
            }
        }

        private void handle(DataInputStream input, DataOutputStream output) throws IOException {
            long magic = input.readLong();
            if (magic != PROTOCOL_IDENTIFIER) {
                Log.w(Logging.LOG_TAG, "Unrecognized header format "
                        + Long.toHexString(magic));
                return;
            }
            int version = input.readInt();

            // Send current protocol version to the IDE so it can decide what to do
            output.writeInt(PROTOCOL_VERSION);

            if (version != PROTOCOL_VERSION) {
                Log.w(Logging.LOG_TAG, "Mismatched protocol versions; app is "
                        + "using version " + PROTOCOL_VERSION + " and tool is using version "
                        + version);
                return;
            }

            Fastdex fastdex = Fastdex.get(context);

            while (true) {
                int message = input.readInt();
                switch (message) {
                    case MESSAGE_EOF: {
                        if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                            Log.v(Logging.LOG_TAG, "Received EOF from the IDE");
                        }
                        return;
                    }

                    case MESSAGE_PING: {
                        // Send an "ack" back to the IDE.
                        // The value of the boolean is true only when the app is in the
                        // foreground.
                        boolean active = Restarter.getForegroundActivity(context) != null;
                        output.writeBoolean(active);

                        long buildMillis = fastdex.getRuntimeMetaInfo().getBuildMillis();
                        output.writeLong(buildMillis);
                        String variantName = fastdex.getRuntimeMetaInfo().getVariantName();
                        output.writeUTF(variantName);
                        output.writeInt(android.os.Process.myPid());
                        if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                            Log.v(Logging.LOG_TAG, "Received Ping message from the IDE; " + "returned active = " + active);
                        }
                        continue;
                    }

                    case MESSAGE_PING_AND_SHOW_TOAST: {
                        // Send an "ack" back to the IDE.
                        // The value of the boolean is true only when the app is in the
                        // foreground.
                        boolean active = Restarter.getForegroundActivity(context) != null;
                        output.writeBoolean(active);

                        RuntimeMetaInfo runtimeMetaInfo = fastdex.getRuntimeMetaInfo();

                        output.writeLong(runtimeMetaInfo.getBuildMillis());
                        output.writeUTF(runtimeMetaInfo.getVariantName());

                        output.writeInt(runtimeMetaInfo.getResourcesVersion());
                        output.writeInt(runtimeMetaInfo.getPatchDexVersion());
                        output.writeInt(runtimeMetaInfo.getMergedDexVersion());

                        if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                            Log.v(Logging.LOG_TAG, "Received Ping message from the IDE; " + "returned active = " + active);
                        }

                        String text = input.readUTF();

                        if (text != null && !TextUtils.isEmpty(text.trim())) {
                            showToast(text,context);
                        }
                        continue;
                    }

                    case MESSAGE_PATCHES: {
                        if (!authenticate(input)) {
                            return;
                        }

                        try {
                            showToast("fastdex, receiving patch info...",context);
                        } catch (Throwable e) {

                        }
                        List<ApplicationPatch> changes = ApplicationPatch.read(input);
                        if (changes == null) {
                            continue;
                        }

                        boolean hasDex = hasDex(changes);
                        boolean hasResources = false;

                        String resourcesApkPath = null;
                        for (ApplicationPatch change : changes) {
                            String path = change.getPath();
                            if (isResourcePath(path)) {
                                hasResources = true;
                                resourcesApkPath = path;
                            }

                        }

                        int updateMode = input.readInt();
                        updateMode = handlePatches(changes, hasResources, updateMode);

                        boolean showToast = input.readBoolean();

                        // Send an "ack" back to the IDE; this is used for timing purposes only
                        output.writeBoolean(true);

                        restart(updateMode,hasDex, hasResources,resourcesApkPath, showToast);

//                        try {
//                            Thread.sleep(300);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }

                        output.writeBoolean(true);
                        continue;
                    }

                    case MESSAGE_PATH_EXISTS: {

                        continue;
                    }

                    case MESSAGE_PATH_CHECKSUM: {

                        continue;
                    }

                    case MESSAGE_RESTART_ACTIVITY: {
                        if (!authenticate(input)) {
                            return;
                        }

                        Activity activity = Restarter.getForegroundActivity(context);
                        if (activity != null) {
                            if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                                Log.v(Logging.LOG_TAG, "Restarting activity per user request");
                            }
                            Restarter.restartActivityOnUiThread(activity);
                        }
                        continue;
                    }

                    case MESSAGE_SHOW_TOAST: {
                        String text = input.readUTF();
                        showToast(text,context);
                        continue;
                    }

                    default: {
                        if (Log.isLoggable(Logging.LOG_TAG, Log.ERROR)) {
                            Log.e(Logging.LOG_TAG, "Unexpected message type: " + message);
                        }
                        // If we hit unexpected message types we can't really continue
                        // the conversation: we can misinterpret data for the unexpected
                        // command as separate messages with different meanings than intended
                        return;
                    }
                }
            }
        }

        private boolean authenticate(DataInputStream input) throws IOException {
            long token = input.readLong();
            if (token != AppInfo.token) {
                Log.w(Logging.LOG_TAG, "Mismatched identity token from client; received " + token
                        + " and expected " + AppInfo.token);
                wrongTokenCount++;
                return false;
            }
            return true;
        }
    }

    public static void showToast(String text,Context context) {
        Activity foreground = Restarter.getForegroundActivity(context);
        if (foreground != null) {
            Restarter.showToast(foreground, text);
        } else if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
            Log.v(Logging.LOG_TAG, "Couldn't show toast (no activity) : " + text);
        }
    }

    public static boolean isResourcePath(String path) {
        return path.endsWith(Constants.RES_SPLIT_STR + Constants.RESOURCE_APK_FILE_NAME);
        //return path.equals(Constants.RESOURCE_APK_FILE_NAME) || path.equals(Paths.RESOURCE_FILE_NAME) || path.startsWith("res/");
    }

    private static boolean hasResources(List<ApplicationPatch> changes) {
        // Any non-code patch is a resource patch (normally resources.ap_ but could
        // also be individual resource files such as res/layout/activity_main.xml)
        for (ApplicationPatch change : changes) {
            String path = change.getPath();
            if (isResourcePath(path)) {
                return true;
            }

        }
        return false;
    }

    public static boolean isDexPath(String path) {
        return path.endsWith(Constants.RES_SPLIT_STR + Constants.PATCH_DEX) || path.endsWith(Constants.RES_SPLIT_STR + Constants.MERGED_PATCH_DEX);
    }

    private static boolean hasDex(List<ApplicationPatch> changes) {
        // Any non-code patch is a resource patch (normally resources.ap_ but could
        // also be individual resource files such as res/layout/activity_main.xml)
        for (ApplicationPatch change : changes) {
            String path = change.getPath();
            if (isDexPath(path)) {
                return true;
            }

        }
        return false;
    }

    private int handlePatches(List<ApplicationPatch> changes, boolean hasResources,
            int updateMode) {
//        if (hasResources) {
//            FileManager.startUpdate();
//        }
//
//        for (ApplicationPatch change : changes) {
//            String path = change.getPath();
//            if (path.equals(Paths.RELOAD_DEX_FILE_NAME)) {
//                updateMode = handleHotSwapPatch(updateMode, change);
//            } else if (isResourcePath(path)) {
//                updateMode = handleResourcePatch(updateMode, change, path);
//            }
//        }
//
//        if (hasResources) {
//            FileManager.finishUpdate(true);
//        }
//
//        return updateMode;


        final Fastdex fastdex = Fastdex.get(context);
        final RuntimeMetaInfo runtimeMetaInfo = fastdex.getRuntimeMetaInfo();

        final File workDir = new File(fastdex.getTempDirectory(),System.currentTimeMillis() + "-" + UUID.randomUUID().toString());
        final File patchDir = fastdex.getPatchDirectory();

        final File dexDirectory = new File(patchDir,Constants.DEX_DIR);
        final File resourceDirectory = new File(patchDir,Constants.RES_DIR);

        final File workDexDirectory = new File(workDir,Constants.DEX_DIR);
        final File workResourceDirectory = new File(workDir,Constants.RES_DIR);

        try {

            boolean hasResourcesApk = false;
            boolean hasMergedDex = false;
            boolean hasPatchDex = false;
            for (ApplicationPatch change : changes) {
                String path = change.getPath();
                if (path.endsWith(Constants.DEX_SUFFIX)) {
                    if (path.endsWith(ShareConstants.RES_SPLIT_STR + ShareConstants.MERGED_PATCH_DEX)) {
                        hasMergedDex = true;
                    }
                    if (path.endsWith(ShareConstants.RES_SPLIT_STR + ShareConstants.PATCH_DEX)) {
                        hasPatchDex = true;
                    }
                    updateMode = handleHotSwapPatch(updateMode, change,workDir);
                } else if (isResourcePath(path)) {
                    hasResourcesApk = true;
                    updateMode = handleResourcePatch(updateMode, change, path,workDir);
                }
            }

            File currentResourcesApk = new File(resourceDirectory,ShareConstants.RESOURCE_APK_FILE_NAME);
            if (!hasResourcesApk && runtimeMetaInfo.getResourcesVersion() > 0 && FileUtils.isLegalFile(currentResourcesApk)) {
                FileUtils.copyFileUsingStream(currentResourcesApk,new File(workResourceDirectory,ShareConstants.RESOURCE_APK_FILE_NAME));
            }

            File currentPatchDex = new File(dexDirectory,ShareConstants.PATCH_DEX);
            if (!hasPatchDex && runtimeMetaInfo.getPatchDexVersion() > 0 && FileUtils.isLegalFile(currentPatchDex)) {
                FileUtils.copyFileUsingStream(currentPatchDex,new File(workDexDirectory,ShareConstants.PATCH_DEX));
            }
            File currentMergedPatchDex = new File(dexDirectory,ShareConstants.MERGED_PATCH_DEX);
            if (!hasMergedDex && runtimeMetaInfo.getMergedDexVersion() > 0 && FileUtils.isLegalFile(currentPatchDex)) {
                FileUtils.copyFileUsingStream(currentMergedPatchDex,new File(workDexDirectory,ShareConstants.MERGED_PATCH_DEX));
            }

            runtimeMetaInfo.setPreparedPatchPath(workDir.getAbsolutePath());
            runtimeMetaInfo.save(fastdex);
        } catch (Throwable e) {
            e.printStackTrace();
            return UPDATE_MODE_NONE;
        }
        return updateMode;
    }

    private static int handleResourcePatch(int updateMode, ApplicationPatch patch, String path, File workDir) throws IOException {
        if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
            Log.v(Logging.LOG_TAG, "Received resource changes (" + path + ")");
        }

        FileUtils.write2file(patch.getBytes(),new File(workDir, Constants.RES_DIR + "/" + path));

        //noinspection ResourceType
        updateMode = Math.max(updateMode, UPDATE_MODE_WARM_SWAP);
        return updateMode;
    }

    private int handleHotSwapPatch(int updateMode, ApplicationPatch patch, File workDir) {
        if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
            Log.v(Logging.LOG_TAG, "Received incremental code patch");
        }
        try {
            File tempDexDir = new File(workDir, Constants.DEX_DIR + "/");

            FileUtils.write2file(patch.getBytes(),new File(tempDexDir,patch.getPath()));
        } catch (Throwable e) {
            Log.e(Logging.LOG_TAG, "Couldn't apply code changes", e);
            updateMode = UPDATE_MODE_COLD_SWAP;
        }
        return UPDATE_MODE_COLD_SWAP;
    }

    public static String[] splitPatchPath(String path) {
        return path.split(Constants.RES_SPLIT_STR);
    }

    private void restart(int updateMode, boolean hasDex, boolean incrementalResources, String resourcesApkPath, boolean toast) {
        if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
            Log.v(Logging.LOG_TAG, "Finished loading changes; update mode =" + updateMode);
        }

        if (updateMode == UPDATE_MODE_NONE || updateMode == UPDATE_MODE_HOT_SWAP) {
            if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                Log.v(Logging.LOG_TAG, "Applying incremental code without restart");
            }

            if (toast) {
                Activity foreground = Restarter.getForegroundActivity(context);
                if (foreground != null) {
                    Restarter.showToast(foreground, "Applied code changes without activity restart");
                } else if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                    Log.v(Logging.LOG_TAG, "Couldn't show toast: no activity found");
                }
            }
            return;
        }

       // android.os.Process.killProcess(android.os.Process.myPid());
        final List<Activity> activities = Restarter.getActivities(context, false);

        if (!hasDex && incrementalResources && updateMode == UPDATE_MODE_WARM_SWAP) {
            // Try to just replace the resources on the fly!

            File resDir = new File(Fastdex.get(context).getRuntimeMetaInfo().getPreparedPatchPath(),Constants.RES_DIR);
            File file = new File(resDir, resourcesApkPath);
            Log.v(Logging.LOG_TAG, "About to update resource file=" + file +
                    ", activities=" + activities);

            if (FileUtils.isLegalFile(file)) {
                String resources = file.getAbsolutePath();

                MonkeyPatcher.monkeyPatchExistingResources(context, resources, activities);
            } else {
                Log.e(Logging.LOG_TAG, "No resource file found to apply");
                updateMode = UPDATE_MODE_COLD_SWAP;
            }
        }
//        else {
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    for (Activity activity : activities) {
//                        try {
//                            activity.finish();
//                        } catch (Throwable e) {
//
//                        }
//                    }
//
//                    android.os.Process.killProcess(android.os.Process.myPid());
//                    System.exit(0);
//                }
//            });
//        }

        Activity activity = Restarter.getForegroundActivity(context);
        if (updateMode == UPDATE_MODE_WARM_SWAP) {
            if (activity != null) {
                if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                    Log.v(Logging.LOG_TAG, "Restarting activity only!");
                }

                boolean handledRestart = false;
                try {
                    // Allow methods to handle their own restart by implementing
                    //     public boolean onHandleCodeChange(long flags) { .... }
                    // and returning true if the change was handled manually
                    Method method = activity.getClass().getMethod("onHandleCodeChange", Long.TYPE);
                    Object result = method.invoke(activity, 0L);
                    if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                        Log.v(Logging.LOG_TAG, "Activity " + activity
                                + " provided manual restart method; return " + result);
                    }
                    if (Boolean.TRUE.equals(result)) {
                        handledRestart = true;
                        if (toast) {
                            Restarter.showToast(activity, "Applied changes");
                        }
                    }
                } catch (Throwable ignore) {
                }

                if (!handledRestart) {
                    if (toast) {
                        Restarter.showToast(activity, "Applied changes, restarted activity");
                    }
                    Restarter.restartActivityOnUiThread(activity);
                }
                return;
            }

            if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                Log.v(Logging.LOG_TAG, "No activity found, falling through to do a full app restart");
            }
            updateMode = UPDATE_MODE_COLD_SWAP;
        }

        if (updateMode != UPDATE_MODE_COLD_SWAP) {
            if (Log.isLoggable(Logging.LOG_TAG, Log.ERROR)) {
                Log.e(Logging.LOG_TAG, "Unexpected update mode: " + updateMode);
            }
            return;
        }

        if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
            Log.v(Logging.LOG_TAG, "Waiting for app to be killed and restarted by the IDE...");
        }
    }
}
