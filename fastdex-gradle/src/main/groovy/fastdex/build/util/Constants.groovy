package fastdex.build.util

import fastdex.common.ShareConstants

/**
 * Created by tong on 17/3/14.
 */
interface Constants extends ShareConstants {
    String BUILD_DIR = "fastdex"
    String R_TXT = "R.txt"
    String RESOURCE_PUBLIC_XML = "public.xml"
    String RESOURCE_IDX_XML = "idx.xml"
    String RUNTIME_DEX_FILENAME = "fastdex-runtime.dex"
    String DEPENDENCIES_FILENAME = "dependencies.json"
    String SOURCESET_SNAPSHOOT_FILENAME = "sourceSets.json"
    String LAST_DIFF_RESULT_SET_FILENAME = "lastDiffResultSet.json"
    String CLASSPATH_FILENAME = "classpath.json"
    String ANDROID_MANIFEST_FILENAME = "android_manifest.json"
    String ERROR_REPORT_FILENAME = "last-build-error-report.txt"
    String DEFAULT_LIBRARY_VARIANT_DIR_NAME = "release"
    String DEX_MERGE_JAR_FILENAME = "fastdex-dex-merge.jar"
    String STUDIO_INFO_SCRIPT_MACOS = "fastdex-studio-info-macos-%s.sh"
    String MIN_BUILD_CACHE_ENABLED_VERSION = "2.2.2"
    String PATCH_JAR = "patch.jar"
}
