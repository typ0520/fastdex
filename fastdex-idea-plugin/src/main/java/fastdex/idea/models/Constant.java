package fastdex.idea.models;

/**
 * Created by pengwei on 2016/10/22.
 */
public interface Constant {
    boolean DEBUG_MODE = true;

    // groupId
    String FASTDEX_CLASSPATH_GROUP = "com.github.typ0520";
    // artifactId
    String FASTDEX_CLASSPATH_ARTIFACT = "fastdex-gradle";
    // plugin name
    String FASTDEX_PLUGIN_ID = "fastdex.app";

    // gradle tool
    String ANDROID_GRADLE_TOOL_GROUP_NAME = "com.android.tools.build";

    //插件依赖的最低的fastdex版本
    String MIN_FASTDEX_VERSION = "0.3";

    //最低支持多个设备连接的fastdex版本
    String MIN_SUPPORT_MULTIPLE_DEVICE_FASTDEX_VERSION = "0.4";
}
