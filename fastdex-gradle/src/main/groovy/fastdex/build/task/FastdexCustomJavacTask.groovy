package fastdex.build.task

import fastdex.build.lib.snapshoot.sourceset.PathInfo
import fastdex.build.lib.snapshoot.sourceset.SourceSetDiffResultSet
import fastdex.build.util.Constants
import fastdex.build.util.FastdexRuntimeException
import fastdex.build.util.FastdexUtils
import fastdex.common.ShareConstants
import fastdex.common.utils.FileUtils
import fastdex.build.variant.FastdexVariant
import fastdex.common.utils.SerializeUtils
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

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
    Object javaCompile
    Object javacIncrementalSafeguard

    FastdexCustomJavacTask() {
        group = 'fastdex'
    }

    def disableJavaCompile(boolean disable) {
        javaCompile.enabled = !disable

        if (javacIncrementalSafeguard != null) {
            try {
                javacIncrementalSafeguard.enabled = !disable
            } catch (Throwable e) {

            }
        }
    }

    @TaskAction
    void compile() {
        def project = fastdexVariant.project
        def projectSnapshoot = fastdexVariant.projectSnapshoot

        File classesDir = fastdexVariant.androidVariant.getVariantData().getScope().getJavaOutputDir()
        if (!FileUtils.dirExists(classesDir.absolutePath)) {
            project.logger.error("==fastdex classes dir not exists, just ignore")
            return
        }

        if (!fastdexVariant.configuration.useCustomCompile) {
            project.logger.error("==fastdex useCustomCompile is false, just ignore")
            return
        }

        if (!fastdexVariant.hasDexCache) {
            project.logger.error("==fastdex dex cache not exists, just ignore")
            return
        }

        SourceSetDiffResultSet sourceSetDiffResultSet = projectSnapshoot.diffResultSet
        //java文件是否发生变化
        if (!sourceSetDiffResultSet.isJavaFileChanged()) {
            project.logger.error("==fastdex no java files changed, just ignore")
            disableJavaCompile(true)
            return
        }

        //此次变化是否和上次的变化一样
        if (projectSnapshoot.diffResultSet != null
                && projectSnapshoot.oldDiffResultSet != null
                && projectSnapshoot.diffResultSet.equals(projectSnapshoot.oldDiffResultSet)) {
            project.logger.error("==fastdex java files not changed, just ignore")
            disableJavaCompile(true)
            return
        }

        Set<PathInfo> addOrModifiedPathInfos = new HashSet<>()

        for (PathInfo pathInfo : sourceSetDiffResultSet.addOrModifiedPathInfosMap.get(project.projectDir.absolutePath)) {
            if (pathInfo.relativePath.endsWith(ShareConstants.JAVA_SUFFIX)) {
                addOrModifiedPathInfos.add(pathInfo)
            }
            else {
                project.logger.error("==fastdex skip kotlin file: ${pathInfo.relativePath}")
            }
        }

        if (addOrModifiedPathInfos.isEmpty()) {
            project.logger.error("==fastdex no java files changed, just ignore")
            disableJavaCompile(true)
            return
        }

        //compile java
        File androidJar = new File("${FastdexUtils.getSdkDirectory(project)}${File.separator}platforms${File.separator}${project.android.getCompileSdkVersion()}${File.separator}android.jar")

        def classpath = new ArrayList()
        classpath.add(classesDir.absolutePath)
        classpath.add(androidJar.absolutePath)

        File classpathFile = new File(FastdexUtils.getBuildDir(project,fastdexVariant.variantName),Constants.CLASSPATH_FILENAME)
        ArrayList<String> list = SerializeUtils.load(new FileInputStream(classpathFile), ArrayList.class)
        classpath.addAll(list)

        def executable = FastdexUtils.getJavacCmdPath()
        project.logger.error("==fastdex executable ${executable}")
        //处理retrolambda
        if (project.plugins.hasPlugin("me.tatarka.retrolambda")) {
            //def retrolambda = project.extensions.getByType(RetrolambdaExtension)
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
        cmdArgs.add("-cp")
        cmdArgs.add(joinClasspath(classpath))

        for (PathInfo pathInfo : addOrModifiedPathInfos) {
            if (pathInfo.relativePath.endsWith(ShareConstants.JAVA_SUFFIX)) {
                project.logger.error("==fastdex changed java file: ${pathInfo.relativePath}")
                cmdArgs.add(pathInfo.absoluteFile.absolutePath)
            }
            else {
                project.logger.error("==fastdex skip kotlin file: ${pathInfo.relativePath}")
            }
        }

        def aptConfiguration = project.configurations.findByName("apt")
        def isAptEnabled = project.plugins.hasPlugin("android-apt") && aptConfiguration != null && !aptConfiguration.empty

        def annotationProcessorConfig = project.configurations.findByName("annotationProcessor")
        def isAnnotationProcessor = annotationProcessorConfig != null && !annotationProcessorConfig.empty

        if ((isAptEnabled || isAnnotationProcessor) && javaCompile) {
            project.logger.error("==fastdex found ${project.name} apt plugin enabled.")

            def aptOutputDir
            if (project.plugins.hasPlugin("com.android.application")) {
                aptOutputDir = new File(project.buildDir, "generated/source/apt/${fastdexVariant.androidVariant.dirName}").absolutePath
            } else {
                aptOutputDir = new File(project.buildDir, "generated/source/apt/release").absolutePath
            }

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

            cmdArgs.add("-s")
            cmdArgs.add(aptOutputDir)

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

        cmdArgs.add("-d")
        cmdArgs.add(classesDir.absolutePath)

        StringBuilder cmd = new StringBuilder()
        String[] cmdArr = new String[cmdArgs.size()]
        for (int i = 0; i < cmdArgs.size(); i++) {
            cmdArr[i] = cmdArgs.get(i)
            cmd.append(" " + cmdArgs.get(i))
        }

        long start = System.currentTimeMillis()

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArr)
        def process = processBuilder.start()

        InputStream is = process.getInputStream()
        BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        String line = null
        while ((line = reader.readLine()) != null) {
            println(line)
        }
        reader.close()

        int status = process.waitFor()

        reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();

        try {
            process.destroy()
        } catch (Throwable e) {

        }

        if (status != 0) {
            throw new FastdexRuntimeException("==fastdex javac fail: \n${cmd}")
        }
        else {
            long end = System.currentTimeMillis()
            if (project.fastdex.debug) {
                project.logger.error("${cmd}")
            }
            project.logger.error("==fastdex javac success, use: ${end - start}ms")
        }
        disableJavaCompile(true)
        //保存对比信息
        fastdexVariant.projectSnapshoot.saveDiffResultSet()
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
