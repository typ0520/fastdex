package fastdex.common;

/**
 * Created by tong on 17/4/28.
 */
public interface ShareConstants {
    String JAVA_SUFFIX = ".java";
    String KT_SUFFIX = ".kt";
    String CLASS_SUFFIX = ".class";
    String DEX_SUFFIX = ".dex";
    String CLASSES = "classes";
    String CLASSES_DEX = CLASSES + DEX_SUFFIX;
    String META_INFO_FILENAME = "fastdex-meta-info.json";
    String RESOURCE_APK_FILE_NAME = "resources.apk";
    String MERGED_PATCH_DEX = "merged-patch.dex";
    String PATCH_DEX = "patch.dex";
    String RES_SPLIT_STR = "__";
    long MESSAGE_TOKEN = 0x19910520L;
}
