package com.dx168.fastdex.build.util;

/**
 * Created by tong on 17/3/14.
 */
public interface Constant {
    /**
     * 最低支持的android gradle build版本
     */
    String MIN_SUPPORT_ANDROID_GRADLE_VERSION = "2.0.0"
    String BUILD_DIR = "fastdex"
    String DEX_CACHE_DIR = "dex_cache"
    String INJECTED_JAR_FILENAME = "injected-combined.jar"
    String JAVA_SUFFIX = ".java"
    String CLASS_SUFFIX = ".class"
    String DEX_SUFFIX = ".dex"
    String R_TXT = "R.txt"
    String CLASSES = "classes"
    String CLASSES_DEX = "${CLASSES}${DEX_SUFFIX}"
    String TAG_ALREADY_EXEC_DEX_MERGE = "tag_already_exec_dex_merge"
    String RUNTIME_DEX_FILENAME = "com.dx168.fastdex.runtime.dex"
    String DEX_MERGE_JAR_FILENAME = "fastdex-dex-merge.jar"
    String DEPENDENCIES_MAPPING_FILENAME = "dependencies-mapping.txt"
    String SOURCESET_SNAPSHOOT_FILENAME = "sourceSets.json"
    String LAST_DIFF_RESULT_SET_FILENAME = "lastDiffResultSet.json"
}
