package com.dx168.fastdex.build.util

import fastdex.common.ShareConstants;

/**
 * Created by tong on 17/3/14.
 */
public interface Constants extends ShareConstants {
    /**
     * 最低支持的android gradle build版本
     */
    String MIN_SUPPORT_ANDROID_GRADLE_VERSION = "2.0.0"
    String BUILD_DIR = "fastdex"
    String INJECTED_JAR_FILENAME = "injected-combined.jar"
    String R_TXT = "r.txt"
    String RESOURCE_PUBLIC_XML = "public.xml"
    String RESOURCE_IDX_XML = "idx.xml"
    String RUNTIME_DEX_FILENAME = "fastdex-runtime.dex"
    String DEPENDENCIES_FILENAME = "dependencies.json"
    String SOURCESET_SNAPSHOOT_FILENAME = "sourceSets.json"
    String LAST_DIFF_RESULT_SET_FILENAME = "lastDiffResultSet.json"
    String ERROR_REPORT_FILENAME = "last-build-error-report.txt"
    String DEFAULT_LIBRARY_VARIANT_DIR_NAME = "release"

    String DEX_MERGE_JAR_FILENAME = "fastdex-dex-merge.jar"
    String STUDIO_INFO_SCRIPT_MACOS = "fastdex-studio-info-macos-%s.sh"
}
