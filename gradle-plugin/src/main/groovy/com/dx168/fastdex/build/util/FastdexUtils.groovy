package com.dx168.fastdex.build.util

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project

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
     * 获取fastdex的build目录
     * @param project
     * @return
     */
    public static final File getBuildDir(Project project) {
        File file = new File(project.getBuildDir(),Constant.BUILD_DIR);
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
     * 获取指定variantName的dex缓存目录
     * @param project
     * @return
     */
    public static final File getDexCacheDir(Project project,String variantName) {
        File file = new File(getBuildDir(project,variantName),Constant.DEX_CACHE_DIR);
        return file;
    }

    /**
     * 获取指定variantName的源码目录快照
     * @param project
     * @return
     */
    public static final File getSourceSetSnapshootFile(Project project, String variantName) {
        File file = new File(getBuildDir(project,variantName),Constant.SOURCESET_SNAPSHOOT_FILENAME);
        return file;
    }

    /**
     * 是否存在dex缓存
     * @param project
     * @param variantName
     * @return
     */
    public static boolean hasDexCache(Project project, String variantName) {
        File cacheDexDir = getDexCacheDir(project,variantName)
        if (!FileUtils.dirExists(cacheDexDir.getAbsolutePath())) {
            return false;
        }

        //check dex
        boolean result = false
        for (File file : cacheDexDir.listFiles()) {
            if (file.getName().endsWith(Constant.DEX_SUFFIX)) {
                result = true
                break
            }
        }
        //check R.txt
        return result
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
    public static File getCachedResourceMappingFile(Project project,String variantName) {
        File resourceMappingFile = new File(getBuildDir(project,variantName),Constant.R_TXT)
        return resourceMappingFile
    }

    /**
     * 获取全量打包时的依赖列表
     * @param project
     * @param variantName
     * @return
     */
    public static File getCachedDependListFile(Project project,String variantName) {
        File cachedDependListFile = new File(getBuildDir(project,variantName),Constant.DEPENDENCIES_MAPPING_FILENAME)
        return cachedDependListFile
    }

    /**
     * 获取缓存的java文件对比结果文件
     * @param project
     * @param variantName
     * @return
     */
    public static File getDiffResultSetFile(Project project,String variantName) {
        File diffResultFile = new File(getBuildDir(project,variantName),Constant.LAST_DIFF_RESULT_SET_FILENAME)
        return diffResultFile
    }

    /**
     * 获取全量打包时的包括所有代码的jar包
     * @param project
     * @param variantName
     * @return
     */
    public static File getInjectedJarFile(Project project,String variantName) {
        File injectedJarFile = new File(getBuildDir(project,variantName),Constant.INJECTED_JAR_FILENAME)
        return injectedJarFile
    }

    /**
     * 获取所有编译的class存放目录
     * @param invocation
     * @return
     */
    public static Set<File> getDirectoryInputFiles(TransformInvocation invocation) {
        Set<File> dirClasspaths = new HashSet<>();
        for (TransformInput input : invocation.getInputs()) {
            Collection<DirectoryInput> directoryInputs = input.getDirectoryInputs()
            if (directoryInputs != null) {
                for (DirectoryInput directoryInput : directoryInputs) {
                    dirClasspaths.add(directoryInput.getFile())
                }
            }
        }

        return dirClasspaths
    }

    /**
     * 获取缓存的依赖列表
     * @param project
     * @param variantName
     * @return
     */
    public static Set<String> getCachedDependList(Project project,String variantName) {
        Set<String> result = new HashSet<>()
        File cachedDependListFile = FastdexUtils.getCachedDependListFile(project,variantName)
        if (FileUtils.isLegalFile(cachedDependListFile)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cachedDependListFile)))
            String line = null
            while ((line = reader.readLine()) != null) {
                result.add(line)
            }
            reader.close()
        }
        return result
    }
}
