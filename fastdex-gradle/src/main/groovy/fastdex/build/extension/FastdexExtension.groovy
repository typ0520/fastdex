package fastdex.build.extension

/**
 * Created by tong on 17/10/3.
 */
public class FastdexExtension {
    /**
     * 是否可用
     */
    boolean fastdexEnable = true
    /**
     * debug模式下打印的日志稍微多一些
     */
    boolean debug = false
    /**
     * 是否换成fastdex的编译方式
     */
    boolean useCustomCompile = true
    /**
     * 每次都参与dex生成的class
     */
    String[] hotClasses = []
    /**
     * 当变化的java文件数量超过阈值,触发dex merge
     */
    int dexMergeThreshold = 4

    /**
     * 目前当只有资源改变时不会重启app，需要这个字段设置为true，那么每次补丁发过去都会重启app
     */
    boolean forceRebootApp = false

    /**
     * 目前只有开启databinding是才会监控build/generated/source/apt目录，如果设置为true任何时候都会监控
     */
    boolean traceApt = false

    /**
     * 是否使用buildCache
     */
    boolean useBuildCache = true
}