/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fastdex.runtime.loader.shareutil;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import java.io.File;
import fastdex.common.ShareConstants;
import fastdex.runtime.Constants;

public class SharePatchFileUtil {
    /**
     * data dir, such as /data/data/com.github.typ0520.fastdex.sample/fastdex
     * @param context
     * @return
     */
    public static File getFastdexDirectory(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (applicationInfo == null) {
            // Looks like running on a test Context, so just return without patching.
            return null;
        }

        return new File(applicationInfo.dataDir, Constants.FASTDEX_DIR);
    }


    /**
     * data dir, such as /data/data/com.github.typ0520.fastdex.sample/fastdex/patch
     * @param context
     * @return
     */
    public static File getPatchDirectory(Context context) {
        return new File(getFastdexDirectory(context), Constants.PATCH_DIR);
    }

    public static File getPatchTempDirectory(Context context) {
        return new File(getFastdexDirectory(context), Constants.TEMP_DIR);
    }

    /**
     * change the jar file path as the makeDexElements do
     *
     * @param path
     * @param optimizedDirectory
     * @return
     */
    public static String optimizedPathFor(File path, File optimizedDirectory) {
        String fileName = path.getName();
        if (!fileName.endsWith(ShareConstants.DEX_SUFFIX)) {
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot < 0) {
                fileName += ShareConstants.DEX_SUFFIX;
            } else {
                StringBuilder sb = new StringBuilder(lastDot + 4);
                sb.append(fileName, 0, lastDot);
                sb.append(ShareConstants.DEX_SUFFIX);
                fileName = sb.toString();
            }
        }

        File result = new File(optimizedDirectory, fileName);
        return result.getPath();
    }
}

