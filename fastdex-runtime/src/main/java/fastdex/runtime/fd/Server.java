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

import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import fastdex.common.ShareConstants;
import fastdex.common.utils.FileUtils;
import fastdex.runtime.Constants;
import fastdex.runtime.FastdexReceiver;
import fastdex.runtime.Fastdex;
import fastdex.runtime.RuntimeMetaInfo;

import static fastdex.common.fd.ProtocolConstants.MESSAGE_EOF;
import static fastdex.common.fd.ProtocolConstants.MESSAGE_PATCHES;
import static fastdex.common.fd.ProtocolConstants.MESSAGE_PING;
import static fastdex.common.fd.ProtocolConstants.MESSAGE_PING_AND_SHOW_TOAST;
import static fastdex.common.fd.ProtocolConstants.MESSAGE_SHOW_TOAST;
import static fastdex.common.fd.ProtocolConstants.PROTOCOL_IDENTIFIER;
import static fastdex.common.fd.ProtocolConstants.PROTOCOL_VERSION;
import static fastdex.common.fd.ProtocolConstants.UPDATE_MODE_COLD_SWAP;
import static fastdex.common.fd.ProtocolConstants.UPDATE_MODE_NONE;
import static fastdex.common.fd.ProtocolConstants.UPDATE_MODE_WARM_SWAP;

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

    private static int wrongTokenCount;


    public static Server create(Context context) {
        return new Server(context.getPackageName());
    }

    private Server(String packageName) {
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

            Fastdex fastdex = Fastdex.get();

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
                        RuntimeMetaInfo runtimeMetaInfo = fastdex.readRuntimeMetaInfoFromFile();
                        output.writeBoolean(runtimeMetaInfo.isActive());

                        long buildMillis = fastdex.readRuntimeMetaInfoFromFile().getBuildMillis();
                        output.writeLong(buildMillis);
                        String variantName = fastdex.readRuntimeMetaInfoFromFile().getVariantName();
                        output.writeUTF(variantName);
                        output.writeInt(android.os.Process.myPid());
                        if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                            Log.v(Logging.LOG_TAG, "Received Ping message from the IDE; " + "returned active = " + runtimeMetaInfo.isActive());
                        }
                        continue;
                    }

                    case MESSAGE_PING_AND_SHOW_TOAST: {
                        // Send an "ack" back to the IDE.
                        // The value of the boolean is true only when the app is in the
                        // foreground.
                        RuntimeMetaInfo runtimeMetaInfo = fastdex.readRuntimeMetaInfoFromFile();


                        output.writeBoolean(runtimeMetaInfo.isActive());
                        output.writeLong(runtimeMetaInfo.getBuildMillis());
                        output.writeUTF(runtimeMetaInfo.getVariantName());

                        output.writeInt(runtimeMetaInfo.getResourcesVersion());
                        output.writeInt(runtimeMetaInfo.getPatchDexVersion());
                        output.writeInt(runtimeMetaInfo.getMergedDexVersion());

                        if (Log.isLoggable(Logging.LOG_TAG, Log.VERBOSE)) {
                            Log.v(Logging.LOG_TAG, "Received Ping message from the IDE; " + "returned active = " + runtimeMetaInfo.isActive());
                        }

                        String text = input.readUTF();

                        if (text != null && !TextUtils.isEmpty(text.trim())) {
                            showToast(text);
                        }
                        continue;
                    }

                    case MESSAGE_PATCHES: {
                        if (!authenticate(input)) {
                            return;
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
                        final RuntimeMetaInfo runtimeMetaInfo = fastdex.readRuntimeMetaInfoFromFile();
                        Object[] objects = handlePatches(runtimeMetaInfo, changes, hasResources, updateMode);

                        updateMode = (Integer) objects[0];
                        String preparedPatchPath = (String) objects[1];
                        runtimeMetaInfo.setPreparedPatchPath(preparedPatchPath);
                        Fastdex.get().saveRuntimeMetaInfo(runtimeMetaInfo);

                        boolean showToast = input.readBoolean();
                        boolean restartAppByCmd = input.readBoolean();
                        FastdexReceiver.restart(updateMode,hasDex, hasResources, preparedPatchPath,resourcesApkPath, showToast, restartAppByCmd);
                        output.writeBoolean(true);
                        continue;
                    }

                    case MESSAGE_SHOW_TOAST: {
                        String text = input.readUTF();
                        showToast(text);
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
            if (token != ShareConstants.MESSAGE_TOKEN) {
                Log.w(Logging.LOG_TAG, "Mismatched identity token from client; received " + token
                        + " and expected " + ShareConstants.MESSAGE_TOKEN);
                wrongTokenCount++;
                return false;
            }
            return true;
        }
    }

    public static void showToast(String text) {
        FastdexReceiver.showToast(text);
    }

    public static boolean isResourcePath(String path) {
        return path.endsWith(Constants.RES_SPLIT_STR + Constants.RESOURCE_APK_FILE_NAME) || path.startsWith("res/");
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

    private Object[] handlePatches(RuntimeMetaInfo runtimeMetaInfo, List<ApplicationPatch> changes, boolean hasResources, int updateMode) {
        final Fastdex fastdex = Fastdex.get();

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

                        int version = Fastdex.parseVersionFromPath(path);
                        runtimeMetaInfo.setMergedDexVersion(version);
                    }
                    if (path.endsWith(ShareConstants.RES_SPLIT_STR + ShareConstants.PATCH_DEX)) {
                        hasPatchDex = true;

                        int version = Fastdex.parseVersionFromPath(path);
                        runtimeMetaInfo.setPatchDexVersion(version);
                    }
                    int localUpdateMode = handleHotSwapPatch(updateMode, change,workDir);
                    if (localUpdateMode > updateMode) {
                        updateMode = localUpdateMode;
                    }
                } else if (isResourcePath(path)) {
                    hasResourcesApk = true;
                    int localUpdateMode = handleResourcePatch(updateMode, change, path,workDir);
                    if (localUpdateMode > updateMode) {
                        updateMode = localUpdateMode;
                    }

                    int version = Fastdex.parseVersionFromPath(path);
                    runtimeMetaInfo.setResourcesVersion(version);
                }
            }

            File currentResourcesApk = new File(resourceDirectory,ShareConstants.RESOURCE_APK_FILE_NAME);
            if (!hasResourcesApk && runtimeMetaInfo.getResourcesVersion() > 0 && FileUtils.isLegalFile(currentResourcesApk)) {
                FileUtils.copyFileUsingStream(currentResourcesApk,new File(workResourceDirectory,ShareConstants.RESOURCE_APK_FILE_NAME));
            }

            if (!(hasMergedDex && !hasPatchDex)) {
                //如果只有merged-dex 没有patch.dex，说明是触发dex merge时发送过来的补丁
                File currentPatchDex = new File(dexDirectory,ShareConstants.PATCH_DEX);
                if (!hasPatchDex && runtimeMetaInfo.getPatchDexVersion() > 0 && FileUtils.isLegalFile(currentPatchDex)) {
                    //Log.d(Logging.LOG_TAG,"复制patch.dex hasPatchDex: " + hasPatchDex + " hasMergedDex: " + hasMergedDex);
                    FileUtils.copyFileUsingStream(currentPatchDex,new File(workDexDirectory,ShareConstants.PATCH_DEX));
                }
            }

            File currentMergedPatchDex = new File(dexDirectory,ShareConstants.MERGED_PATCH_DEX);
            if (!hasMergedDex && runtimeMetaInfo.getMergedDexVersion() > 0 && FileUtils.isLegalFile(currentMergedPatchDex)) {
                FileUtils.copyFileUsingStream(currentMergedPatchDex,new File(workDexDirectory,ShareConstants.MERGED_PATCH_DEX));
            }
        } catch (Throwable e) {
            e.printStackTrace();

            Log.d(Logging.LOG_TAG,"exception when handlePatches: " + e.getMessage());
            return new Object[]{UPDATE_MODE_NONE,workDir.getAbsolutePath()};
        }
        return new Object[]{updateMode,workDir.getAbsolutePath()};
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
            if (patch.getBytes() != null && patch.getBytes().length > 0) {
                FileUtils.write2file(patch.getBytes(),new File(tempDexDir,patch.getPath()));
            }
        } catch (Throwable e) {
            Log.e(Logging.LOG_TAG, "Couldn't apply code changes", e);
        }
        return UPDATE_MODE_COLD_SWAP;
    }
}
