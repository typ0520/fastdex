package fastdex.build.task

import fastdex.build.lib.snapshoot.sourceset.PathInfo
import fastdex.build.lib.snapshoot.sourceset.SourceSetDiffResultSet
import fastdex.build.util.Constants
import fastdex.build.util.FastdexRuntimeException
import fastdex.build.util.FastdexUtils
import fastdex.build.util.GradleUtils
import fastdex.common.ShareConstants
import fastdex.common.utils.FileUtils
import fastdex.build.variant.FastdexVariant
import fastdex.common.utils.SerializeUtils
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * 每次SourceSet下的某个java文件变化时，默认的compile${variantName}JavaWithJavac任务会扫描所有的java文件
 * 处理javax.annotation.processing.AbstractProcessor接口用来代码动态代码生成，所以项目中的java文件如果很多会造成大量的时间浪费
 *
 * 全量打包时使用默认的任务，补丁打包使用此任务以提高效率(仅编译变化的java文件不去扫描代码内容)
 *
 * https://ant.apache.org/manual/Tasks/javac.html
 *
 * Created by tong on 17/3/12.
 */
public class FastdexCustomJavacTask extends DefaultTask {
    FastdexVariant fastdexVariant
    Task javaCompile
    Task javacIncrementalSafeguard
    Task javaPreCompile

    FastdexCustomJavacTask() {
        group = 'fastdex'
    }

    def disableJavaCompile() {
        javaCompile.enabled = false

        if (javacIncrementalSafeguard != null) {
            try {
                javacIncrementalSafeguard.enabled = false
            } catch (Throwable e) {

            }
        }
        if (javaPreCompile != null) {
            try {
                javaPreCompile.enabled = false
            } catch (Throwable e) {

            }
        }
    }

    @TaskAction
    void compile() {
        if (!fastdexVariant.hasDexCache) {
            project.logger.error("==fastdex miss dex cache, just ignore")
            return
        }
        def project = fastdexVariant.project
        def projectSnapshoot = fastdexVariant.projectSnapshoot

        File classesDir = fastdexVariant.androidVariant.getVariantData().getScope().getJavaOutputDir()

        if (!FileUtils.dirExists(classesDir.absolutePath)) {
            project.logger.error("==fastdex miss classes dir, just ignore")
            return
        }

        SourceSetDiffResultSet sourceSetDiffResultSet = projectSnapshoot.diffResultSet
        //java文件是否发生变化
        if (!sourceSetDiffResultSet.isJavaFileChanged()) {
            project.logger.error("==fastdex no java files changed, just ignore")
            disableJavaCompile()
            return
        }

        //此次变化是否和上次的变化一样
        if (projectSnapshoot.diffResultSet != null
                && projectSnapshoot.oldDiffResultSet != null
                && projectSnapshoot.diffResultSet.equals(projectSnapshoot.oldDiffResultSet)) {
            project.logger.error("==fastdex java files not changed, just ignore")
            disableJavaCompile()
            return
        }

        boolean onlyROrBuildConfig = true
        Set<PathInfo> addOrModifiedPathInfos = new HashSet<>()

        String packageNamePath = fastdexVariant.getOriginPackageName().split("\\.").join(File.separator)
        String rRelativePath = packageNamePath + File.separator + "R.java"
        String buildConfigRelativePath = packageNamePath + File.separator + "BuildConfig.java"

        for (PathInfo pathInfo : sourceSetDiffResultSet.addOrModifiedPathInfosMap.get(project.projectDir.absolutePath)) {
            //忽略掉kotlin文件
            if (pathInfo.relativePath.endsWith(ShareConstants.JAVA_SUFFIX)) {
                addOrModifiedPathInfos.add(pathInfo)

                if (onlyROrBuildConfig && !pathInfo.relativePath.equals(rRelativePath) && !pathInfo.relativePath.equals(buildConfigRelativePath)) {
                    onlyROrBuildConfig = false
                }
            }
            else {
                project.logger.error("==fastdex skip kotlin file: ${pathInfo.relativePath}")
            }
        }

        if (addOrModifiedPathInfos.isEmpty()) {
            project.logger.error("==fastdex no java files changed, just ignore")
            disableJavaCompile()
            return
        }

        //compile java
        File androidJar = new File("${FastdexUtils.getSdkDirectory(project)}${File.separator}platforms${File.separator}${project.android.getCompileSdkVersion()}${File.separator}android.jar")

        //class输出目录
        File patchClassesDir = new File(FastdexUtils.getWorkDir(project,fastdexVariant.variantName),"patch-classes")
        FileUtils.deleteDir(patchClassesDir)
        FileUtils.ensumeDir(patchClassesDir)

        def classpath = new ArrayList()
        classpath.add(classesDir.absolutePath)
        classpath.add(androidJar.absolutePath)

        File classpathFile = new File(FastdexUtils.getBuildDir(project,fastdexVariant.variantName),Constants.CLASSPATH_FILENAME)
        ArrayList<String> list = SerializeUtils.load(new FileInputStream(classpathFile), ArrayList.class)
        classpath.addAll(list)

        def executable = FastdexUtils.getJavacCmdPath()
        //处理retrolambda
        if (project.plugins.hasPlugin("me.tatarka.retrolambda")) {
            def retrolambda = project.retrolambda
            def rt = "${retrolambda.jdk}${File.separator}jre${File.separator}lib${File.separator}rt.jar"
            classpath.add(rt)
            executable = "${retrolambda.tryGetJdk()}${File.separator}bin${File.separator}javac"
            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                executable = "${executable}.exe"
            }
        }

        List<String> cmdArgs = new ArrayList<>()
        cmdArgs.add(executable)
        cmdArgs.add("-encoding")
        cmdArgs.add("UTF-8")
        cmdArgs.add("-g")
        cmdArgs.add("-target")
        cmdArgs.add(javaCompile.targetCompatibility)
        cmdArgs.add("-source")
        cmdArgs.add(javaCompile.sourceCompatibility)

        if (!onlyROrBuildConfig) {
            cmdArgs.add("-cp")
            cmdArgs.add(joinClasspath(classpath))
        }

        def aptOutputDir = GradleUtils.getAptOutputDir(fastdexVariant.androidVariant)
        //R.java 或者 BuildConfig.java不用依赖classpath ，不加-cp、-processor会快一些
        if (!onlyROrBuildConfig) {
            def aptConfiguration = project.configurations.findByName("apt")
            def isAptEnabled = (project.plugins.hasPlugin("android-apt") || project.plugins.hasPlugin("com.neenbedankt.android-apt")) && aptConfiguration != null && !aptConfiguration.empty

            def annotationProcessorConfig = project.configurations.findByName("annotationProcessor")
            def isAnnotationProcessor = annotationProcessorConfig != null && !annotationProcessorConfig.empty

            if ((isAptEnabled || isAnnotationProcessor) && javaCompile) {
                project.logger.error("==fastdex found ${project.name} apt plugin enabled.")

                def configurations = javaCompile.classpath

                if (isAptEnabled) {
                    configurations += aptConfiguration
                }
                if (isAnnotationProcessor) {
                    configurations += annotationProcessorConfig
                }

                def processorPath = configurations.asPath

                boolean disableDiscovery = javaCompile.options.compilerArgs.indexOf('-processorpath') == -1

                int processorIndex = javaCompile.options.compilerArgs.indexOf('-processor')
                def processor = null
                if (processorIndex != -1) {
                    processor = javaCompile.options.compilerArgs.get(processorIndex + 1)
                }

                def aptArgs = []
                javaCompile.options.compilerArgs.each { arg ->
                    if (arg.toString().startsWith('-A')) {
                        aptArgs.add(arg)
                    }
                }

                if (processor) {
                    cmdArgs.add("-processor")
                    cmdArgs.add(processor)
                }

                if (!disableDiscovery) {
                    cmdArgs.add("-processorpath")
                    cmdArgs.add(processorPath)
                }

                cmdArgs.addAll(aptArgs)
            } else {
                project.logger.error("==fastdex doesn't found apt plugin for $project.name")
            }
        }

        cmdArgs.add("-s")
        cmdArgs.add(aptOutputDir)
        cmdArgs.add("-d")
        cmdArgs.add(onlyROrBuildConfig ? classesDir.absolutePath : patchClassesDir.absolutePath)

        for (PathInfo pathInfo : addOrModifiedPathInfos) {
            if (pathInfo.relativePath.endsWith(ShareConstants.JAVA_SUFFIX)) {
                project.logger.error("==fastdex changed java file: ${pathInfo.relativePath}")
                cmdArgs.add(pathInfo.absoluteFile.absolutePath)
            }
            else {
                project.logger.error("==fastdex skip kotlin file: ${pathInfo.relativePath}")
            }
        }

        long start = System.currentTimeMillis()

        FastdexUtils.runCommand(project, cmdArgs)

        if (!onlyROrBuildConfig) {
            //覆盖app/build/intermediates/classes内容
            Files.walkFileTree(patchClassesDir.toPath(),new SimpleFileVisitor<Path>(){
                @Override
                FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relativePath = patchClassesDir.toPath().relativize(file)

                    File destFile = new File(classesDir,relativePath.toString())
                    FileUtils.copyFileUsingStream(file.toFile(),destFile)

                    project.logger.error("==fastdex apply class to ${destFile}")

                    String classRelativePath = relativePath.toString()
                    classRelativePath = classRelativePath.substring(0, classRelativePath.length() - ShareConstants.CLASS_SUFFIX.length());
                    classRelativePath = classRelativePath.replaceAll(Os.isFamily(Os.FAMILY_WINDOWS) ? "\\\\" : File.separator,"\\.");

                    int index = classRelativePath.indexOf("\$")
                    if (index != -1) {
                        sourceSetDiffResultSet.addOrModifiedClasses.add(classRelativePath.substring(0,index))
                    }
                    else {
                        sourceSetDiffResultSet.addOrModifiedClasses.add(classRelativePath)
                    }
                    return FileVisitResult.CONTINUE
                }
            })
        }

        disableJavaCompile()
        //保存对比信息
        fastdexVariant.projectSnapshoot.saveDiffResultSet()
        fastdexVariant.compiledByCustomJavac = true
        long end = System.currentTimeMillis()
        project.logger.error("==fastdex javac success, use: ${end - start}ms")
    }

    def joinClasspath(List<String> collection) {
        StringBuilder sb = new StringBuilder()

        boolean window = Os.isFamily(Os.FAMILY_WINDOWS)
        collection.each { file ->
            sb.append(file)
            if (window) {
                sb.append(";")
            }
            else {
                sb.append(":")
            }
        }
        return sb
    }
}
