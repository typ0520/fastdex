/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;
import java.util.List;
/**
 * Service which starts the Instant Run server; started by the IDE via
 * adb shell am startservice pkg/service
 */
public class InstantRunService extends Service {

    private Server server;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Don't allow anyone to bind to this service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Logging.LOG_TAG, "Starting Instant Run Server for " + getPackageName());

        // Start server, unless we're in a multi-process scenario and this isn't the
        // primary process
        try {
            boolean foundPackage = false;
            int pid = Process.myPid();
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> processes = manager.getRunningAppProcesses();

            boolean startServer;
            if (processes != null && processes.size() > 1) {
                // Multiple processes: look at each, and if the process name matches
                // the package name (for the current pid), it's the main process.
                startServer = false;
                for (RunningAppProcessInfo processInfo : processes) {
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
                server = Server.create(this);
            } else {
                Log.d(Logging.LOG_TAG, "In secondary process: Not starting server");

            }
        } catch (Throwable t) {
            Log.d(Logging.LOG_TAG, "Failed during multi process check", t);
            server = Server.create(this);
        }
    }

    @Override
    public void onDestroy() {
        if (server != null) {
            Log.d(Logging.LOG_TAG, "Stopping Instant Run Server for " + getPackageName());
            server.shutdown();
        }
        super.onDestroy();
    }
}
