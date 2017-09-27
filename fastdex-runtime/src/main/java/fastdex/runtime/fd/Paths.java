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

import java.io.File;

/**
 * Shared path-related logic between Android Studio and the Instant Run server.
 */
public final class Paths {
    /** Temp directory on the device */
    public static final String DEVICE_TEMP_DIR = "/data/local/tmp";

    /** The name of the build timestamp file on the device in the data folder */
    public static final String BUILD_ID_TXT = "build-id.txt";

    /** Name of file to write resource data into, if not extracting resources */
    public static final String RESOURCE_FILE_NAME = "resources.ap_";

    /** Name for reload dex files */
    public static final String RELOAD_DEX_FILE_NAME = "classes.dex.3";

    public static String getMainApkDataDirectory(String applicationId) {
        return "/data/data/" + applicationId;
    }

    public static String getDataDirectory(String applicationId) {
        return "/data/data/" + applicationId + "/files/fastdex-instant-run";
    }

    public static String getDeviceIdFolder(String pkg) {
        return DEVICE_TEMP_DIR + "/" + pkg + "-" + BUILD_ID_TXT;
    }
}
