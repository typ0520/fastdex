package com.dx168.fastdex.build.util

import fastdex.common.ShareConstants
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import fastdex.common.utils.FileUtils

/**
 * Created by tong on 17/3/14.
 */
public class FastdexUtils {
    /**
     * 获取sdk路径
     * @param project
     * @return
     */
    public static final String getSdkDirectory(Project project) {
        String sdkDirectory = project.android.getSdkDirectory()
        if (sdkDirectory.contains("\\")) {
            sdkDirectory = sdkDirectory.replace("\\", "/");
        }
        return sdkDirectory
    }

    /**
     * 获取dx命令路径
     * @param project
     * @return
     */
    public static final String getDxCmdPath(Project project) {
        File dx = new File(FastdexUtils.getSdkDirectory(project),"build-tools${File.separator}${project.android.getBuildToolsVersion()}${File.separator}dx")
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            return "${dx.absolutePath}.bat"
        }
        return dx.getAbsolutePath()
    }

    /**
     * 获取aapt命令路径
     * @param project
     * @return
     */
    public static final String getAaptCmdPath(Project project) {
        File aapt = new File(FastdexUtils.getSdkDirectory(project),"build-tools${File.separator}${project.android.getBuildToolsVersion()}${File.separator}aapt")
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            return "${aapt.absolutePath}.exe"
        }
        return aapt.getAbsolutePath()
    }

    /**
     * 获取adb命令路径
     * @param project
     * @return
     */
    public static final String getAdbCmdPath(Project project) {
        File adb = new File(FastdexUtils.getSdkDirectory(project),"platform-tools${File.separator}adb")
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            return "${adb.absolutePath}.exe"
        }
        return adb.getAbsolutePath()
    }

    /**
     * 获取当前jdk路径
     * @return
     */
    public static final String getCurrentJdk() {
        String javaHomeProp = System.properties.'java.home'
        if (javaHomeProp) {
            int jreIndex = javaHomeProp.lastIndexOf("${File.separator}jre")
            if (jreIndex != -1) {
                return javaHomeProp.substring(0, jreIndex)
            } else {
                return javaHomeProp
            }
        } else {
            return System.getenv("JAVA_HOME")
        }
    }

    /**
     * 获取java命令路径
     * @return
     */
    public static final String getJavaCmdPath() {
        StringBuilder cmd = new StringBuilder(getCurrentJdk())
        if (!cmd.toString().endsWith(File.separator)) {
            cmd.append(File.separator)
        }
        cmd.append("bin${File.separator}java")
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            cmd.append(".exe")
        }
        return new File(cmd.toString()).absolutePath
    }

    /**
     * 是否存在dex缓存
     * @param project
     * @param variantName
     * @return
     */
    public static boolean hasDexCache(Project project, String variantName) {
        File cacheDexDir = getDexCacheDir(project,variantName)
        return FileUtils.hasDex(cacheDexDir)
    }

    /**
     * 获取fastdex的build目录
     * @param project
     * @return
     */
    public static final File getBuildDir(Project project) {
        File file = new File(project.getBuildDir(),Constants.BUILD_DIR);
        return file;
    }

    /**
     * 获取fastdex指定variantName的build目录
     * @param project
     * @return
     */
    public static final File getBuildDir(Project project,String variantName) {
        File file = new File(getBuildDir(project),variantName);
        return file;
    }

    /**
     * 获取fastdex指定variantName的work目录
     * @param project
     * @return
     */
    public static final File getWorkDir(Project project,String variantName) {
        File file = new File(getBuildDir(project,variantName),"work")
        return file;
    }

    /**
     * 获取dex目录
     * @param project
     * @param variantName
     * @return
     */
    public static getDexDir(Project project,String variantName) {
        File file = new File(getBuildDir(project,variantName),"dex");
        return file;
    }

    /**
     * 获取指定variantName的dex缓存目录
     * @param project
     * @return
     */
    public static final File getDexCacheDir(Project project,String variantName) {
        File file = new File(getDexDir(project,variantName),"cache");
        return file;
    }

    /**
     * 获取指定variantName的已合并的补丁dex目录
     * @param project
     * @return
     */
    public static final File getMergedPatchDexDir(Project project,String variantName) {
        File file = new File(getDexDir(project,variantName),"merged-patch");
        return file;
    }

    /**
     * 获取指定variantName的补丁dex目录
     * @param project
     * @return
     */
    public static final File getPatchDexDir(Project project,String variantName) {
        File file = new File(getDexDir(project,variantName),"patch");
        return file;
    }

    /**
     * 获取指定variantName的补丁dex文件
     * @param project
     * @return
     */
    public static final File getPatchDexFile(Project project,String variantName) {
        File file = new File(getPatchDexDir(project,variantName),Constants.CLASSES_DEX);
        return file;
    }

    /**
     * 获取指定variantName的补丁merged-dex文件
     * @param project
     * @param variantName
     * @return
     */
    public static final File getMergedPatchDex(Project project,String variantName) {
        File file = new File(getMergedPatchDexDir(project,variantName),Constants.CLASSES_DEX);
        return file;
    }

    /**
     * 获取指定variantName的源码目录快照
     * @param project
     * @return
     */
    public static final File getSourceSetSnapshootFile(Project project, String variantName) {
        File file = new File(getBuildDir(project,variantName),Constants.SOURCESET_SNAPSHOOT_FILENAME);
        return file;
    }

    /**
     * 清空所有缓存
     * @param project
     * @param variantName
     * @return
     */
    public static boolean cleanCache(Project project,String variantName) {
        File dir = getBuildDir(project,variantName)
        project.logger.error("==fastdex clean dir: ${dir}")
        return FileUtils.deleteDir(dir)
    }

    /**
     * 清空指定variantName缓存
     * @param project
     * @param variantName
     * @return
     */
    public static boolean cleanAllCache(Project project) {
        File dir = getBuildDir(project)
        project.logger.error("==fastdex clean dir: ${dir}")
        return FileUtils.deleteDir(dir)
    }

    /**
     * 获取资源映射文件
     * @param project
     * @param variantName
     * @return
     */
    public static File getResourceMappingFile(Project project, String variantName) {
        File resourceMappingFile = new File(getBuildResourceDir(project,variantName),Constants.R_TXT)
        return resourceMappingFile
    }

    public static File getResourceDir(Project project, String variantName) {
        File resDir = new File(getBuildDir(project,variantName),"res")
        return resDir
    }

    public static File getResourcesApk(Project project, String variantName) {
        File resourcesApk = new File(getResourceDir(project,variantName),ShareConstants.RESOURCE_APK_FILE_NAME)
        return resourcesApk
    }

    /**
     * 获取缓存的idx.xml文件
     * @param project
     * @param variantName
     * @return
     */
    public static File getIdxXmlFile(Project project, String variantName) {
        File idxXmlFile = new File(getBuildResourceDir(project,variantName),Constants.RESOURCE_IDX_XML)
        return idxXmlFile
    }

    /**
     * 获取缓存的public.xml文件
     * @param project
     * @param variantName
     * @return
     */
    public static File getPublicXmlFile(Project project, String variantName) {
        File publicXmlFile = new File(getBuildResourceDir(project,variantName),Constants.RESOURCE_PUBLIC_XML)
        return publicXmlFile
    }

    private static File getBuildResourceDir(Project project, String variantName) {
        return new File(getBuildDir(project,variantName),"r")
    }

    /**
     * 获取全量打包时的依赖列表
     * @param project
     * @param variantName
     * @return
     */
    public static File getCachedDependListFile(Project project,String variantName) {
        File cachedDependListFile = new File(getBuildDir(project,variantName),Constants.DEPENDENCIES_FILENAME)
        return cachedDependListFile
    }

    public static File getMetaInfoFile(Project project,String variantName) {
        File cachedDependListFile = new File(getBuildDir(project,variantName),Constants.META_INFO_FILENAME)
        return cachedDependListFile
    }

    /**
     * 获取缓存的java文件对比结果文件
     * @param project
     * @param variantName
     * @return
     */
    public static File getDiffResultSetFile(Project project,String variantName) {
        File diffResultFile = new File(getBuildDir(project,variantName),Constants.LAST_DIFF_RESULT_SET_FILENAME)
        return diffResultFile
    }

    /**
     * 获取全量打包时的包括所有代码的jar包
     * @param project
     * @param variantName
     * @return
     */
    public static File getInjectedJarFile(Project project,String variantName) {
        File injectedJarFile = new File(getBuildDir(project,variantName),Constants.INJECTED_JAR_FILENAME)
        return injectedJarFile
    }
}
