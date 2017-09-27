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

public class AppInfo {
    // Keep the structure of this class in sync with
    // GenerateInstantRunAppInfoTask#writeAppInfoClass

    private AppInfo() {
    }
    /**
     * The application id of this app (e.g. the package name). Used to pick a unique
     * directory for the app's reloaded resources. (We can't look for it in the manifest,
     * since we need this information very early in the app life cycle, and we don't want
     * to call into the framework and cause more parts of it to be initialized before
     * we've monkey-patched the application class and resource loaders.)
     * <p>
     * (Not final: Will be replaced by byte-code manipulation at build time)
     */
    @SuppressWarnings({"CanBeFinal", "StaticVariableNamingConvention"})
    public static String applicationId = null;

    /**
     * A token assigned to this app at build time. This is used such that the running
     * app socket server can be reasonably sure that it's responding to requests from
     * the IDE.
     */
    @SuppressWarnings("StaticVariableNamingConvention")
    public static long token = 0L;
}
