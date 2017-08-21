package fastdex.build.task

import fastdex.build.lib.snapshoot.sourceset.PathInfo
import fastdex.build.lib.snapshoot.sourceset.SourceSetDiffResultSet
import fastdex.build.util.Constants
import fastdex.build.util.FastdexUtils
import fastdex.common.ShareConstants
import fastdex.common.utils.FileUtils
import fastdex.build.variant.FastdexVariant
import fastdex.common.utils.SerializeUtils
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

    FastdexCustomJavacTask() {
        group = 'fastdex'
    }

    def disableJavaCompile(boolean disable) {

    }

    @TaskAction
    void compile() {
        def javaCompile = fastdexVariant.androidVariant.javaCompile
        disableJavaCompile(false)

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
            javaCompile.enabled = false
            return
        }
        Set<PathInfo> addOrModifiedPathInfos = sourceSetDiffResultSet.addOrModifiedPathInfos

//        File patchJavaFileDir = new File(FastdexUtils.getWorkDir(project,fastdexVariant.variantName),"custom-combind")
//        File patchClassesFileDir = new File(FastdexUtils.getWorkDir(project,fastdexVariant.variantName),"custom-combind-classes")
//        FileUtils.deleteDir(patchJavaFileDir)
//        FileUtils.ensumeDir(patchClassesFileDir)
//
//        for (PathInfo pathInfo : addOrModifiedPathInfos) {
//            if (pathInfo.relativePath.endsWith(ShareConstants.JAVA_SUFFIX)) {
//                project.logger.error("==fastdex changed java file: ${pathInfo.relativePath}")
//                FileUtils.copyFileUsingStream(pathInfo.absoluteFile,new File(patchJavaFileDir,pathInfo.relativePath))
//            }
//            else {
//                project.logger.error("==fastdex skip kotlin file: ${pathInfo.relativePath}")
//            }
//        }

        //compile java
        File androidJar = new File("${FastdexUtils.getSdkDirectory(project)}${File.separator}platforms${File.separator}${project.android.getCompileSdkVersion()}${File.separator}android.jar")
        File classpathJar = FastdexUtils.getInjectedJarFile(project,fastdexVariant.variantName)

        //def classpath = project.files(classpathJar.absolutePath) + javaCompile.classpath +
        //def classpath = project.files(classpathJar.absolutePath)
        def classpath = new ArrayList()
        classpath.add(androidJar.absolutePath)
        classpath.add(classesDir.absolutePath)

        File classpathFile = new File(FastdexUtils.getBuildDir(project,fastdexVariant.variantName),Constants.CLASSPATH_FILENAME)
        ArrayList<String> list = SerializeUtils.load(new FileInputStream(classpathFile), ArrayList.class)
        classpath.addAll(list)

        classpath.add(classpathJar.absolutePath)

        def executable = FastdexUtils.getJavacCmdPath()
        project.logger.error("==fastdex executable ${executable}")
        //处理retrolambda
        if (project.plugins.hasPlugin("me.tatarka.retrolambda")) {
            fork = true
            //def retrolambda = project.extensions.getByType(RetrolambdaExtension)
            def retrolambda = project.retrolambda
            def rt = "$retrolambda.jdk/jre/lib/rt.jar"
            classpath.add(rt)

            executable = "${retrolambda.tryGetJdk()}/bin/javac"
        }

        //https://ant.apache.org/manual/Tasks/javac.html
        //最好检测下项目根目录的gradle.properties文件,是否有这个配置org.gradle.jvmargs=-Dfile.encoding=UTF-8
//        project.ant.javac(
//                srcdir: patchJavaFileDir,
//                destdir: patchClassesFileDir,
//                source: javaCompile.sourceCompatibility,
//                target: javaCompile.targetCompatibility,
//                encoding: 'UTF-8',
//                bootclasspath: androidJar,
//                classpath: joinClasspath(classpath),
//                fork: fork,
//                executable: executable
//        )

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

        ProcessBuilder aaptProcess = new ProcessBuilder(cmdArr)
        def process = aaptProcess.start()
        int status = process.waitFor()
        try {
            process.destroy()
        } catch (Throwable e) {

        }

        long end = System.currentTimeMillis()
        project.logger.error("==fastdex javac success, use: ${end - start}ms")
        if (project.fastdex.debug) {
            project.logger.error("${cmd}")
        }

        if (status != 0) {
            throw new RuntimeException("==fastdex javac fail: \n${cmd}")
        }

        //project.logger.error("==fastdex compile success: ${patchClassesFileDir}")

        //覆盖app/build/intermediates/classes内容
//        Files.walkFileTree(patchClassesFileDir.toPath(),new SimpleFileVisitor<Path>(){
//            @Override
//            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                Path relativePath = patchClassesFileDir.toPath().relativize(file)
//                File destFile = new File(classesDir,relativePath.toString())
//                FileUtils.copyFileUsingStream(file.toFile(),destFile)
//
//                project.logger.error("==fastdex apply class to ${destFile}")
//                return FileVisitResult.CONTINUE
//            }
//        })
        disableJavaCompile(true)
        //保存对比信息
        fastdexVariant.projectSnapshoot.saveDiffResultSet()
    }

    def joinClasspath(List<String> collection) {
        StringBuilder sb = new StringBuilder()
        collection.each { file ->
            sb.append(file)
            sb.append(":")
        }
        if (sb.toString().endsWith(":")) {
            sb.deleteCharAt(sb.length() - 1)
        }
        return sb
    }
}
