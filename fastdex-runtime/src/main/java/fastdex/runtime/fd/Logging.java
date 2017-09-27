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

import android.util.Log;
import java.util.logging.Level;

/**
 * Instant Run runtime logging related code
 */
public class Logging {

    /** Log tag used by instant run runtime */
    public static final String LOG_TAG = "FastdexInstantRun";

    static {
        fastdex.runtime.fd.Log.logging =
                new fastdex.runtime.fd.Log.Logging() {
            @Override
            public void log(Level level, String string) {
                log(level, string, null /* throwable */);
            }

            @Override
            public boolean isLoggable(Level level) {
                if (level == Level.SEVERE) {
                    return Log.isLoggable(LOG_TAG, Log.ERROR);
                } else if (level == Level.FINE) {
                    return Log.isLoggable(LOG_TAG, Log.VERBOSE);
                } else return Log.isLoggable(LOG_TAG, Log.INFO);
            }

            @Override
            public void log(Level level, String string,
                    Throwable throwable) {
                if (level == Level.SEVERE) {
                    if (throwable == null) {
                        Log.e(LOG_TAG, string);
                    } else {
                        Log.e(LOG_TAG, string, throwable);
                    }
                } else if (level == Level.FINE) {
                    if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                        if (throwable == null) {
                            Log.v(LOG_TAG, string);
                        } else {
                            Log.v(LOG_TAG, string, throwable);
                        }
                    }
                } else if (Log.isLoggable(LOG_TAG, Log.INFO)) {
                    if (throwable == null) {
                        Log.i(LOG_TAG, string);
                    } else {
                        Log.i(LOG_TAG, string, throwable);
                    }
                }
            }
        };
    }
}
