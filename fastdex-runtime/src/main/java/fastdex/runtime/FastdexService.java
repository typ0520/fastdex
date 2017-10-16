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

package fastdex.runtime;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import fastdex.runtime.fd.Logging;
import fastdex.runtime.fd.Server;

/**
 * Service which starts the Instant Run server; started by the IDE via
 * adb shell am startservice pkg/service
 */
public class FastdexService extends Service {

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
        Log.d(Logging.LOG_TAG, "Starting fastdex Server for " + getPackageName());
        server = Server.create(this);
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
