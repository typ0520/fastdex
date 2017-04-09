package com.dx168.fastdex.build.extension

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
    boolean useCustomCompile = false
    /**
     * 每次都参与dex生成的class
     */
    String[] hotClasses = ["{package}.R","{package}.BuildConfig"]
}