package fastdex.build.extension

/**
 * Created by tong on 17/10/3.
 */
class FastdexExtension {
    /**
     * 是否可用
     */
    //default true (可以用来区分是开发机器调试，还是jenkins机器打包，如果是开发机器就在local.properties加fastdex.enabled=true)
    //Properties localProperties = new Properties()
    //localProperties.load(project.rootProject.file('local.properties').newDataInputStream())
    //fastdexEnable = Boolean.valueOf(localProperties.getProperty("fastdex.enabled", "false"))
    boolean fastdexEnable = true

    /**
     * debug模式下打印的日志稍微多一些
     */
    boolean debug = false

    /**
     * 是否换成fastdex的编译方式
     */
    boolean useCustomCompile = false

    /**
     * 每次都参与dex生成的class
     */
    String[] hotClasses = []

    /**
     * 当变化的java文件数量大于等于这个值时触发dex merge(随着变化的java文件的增多,补丁打包会越来越慢,dex merge以后当前的状态相当于全量打包以后的状态)
     */
    int dexMergeThreshold = 3

    /**
     * 当发送的补丁中包含dex时会调用 'adb shell am force-stop' 强制重启app
     */
    boolean restartAppByCmd = true

    /**
     * 进hook debug这个build type
     */
    boolean onlyHookDebug = false
}