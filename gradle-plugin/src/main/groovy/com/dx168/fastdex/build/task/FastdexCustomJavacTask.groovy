package com.dx168.fastdex.build.task

import com.dx168.fastdex.build.snapshoot.sourceset.PathInfo
import com.dx168.fastdex.build.snapshoot.sourceset.SourceSetDiffResultSet
import com.dx168.fastdex.build.util.FastdexUtils
import com.dx168.fastdex.build.util.FileUtils
import com.dx168.fastdex.build.variant.FastdexVariant
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
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
    Task compileTask

    FastdexCustomJavacTask() {
        group = 'fastdex'
    }

    @TaskAction
    void compile() {
        //检查缓存的有效性
        fastdexVariant.prepareEnv()

        def project = fastdexVariant.project
        def projectSnapshoot = fastdexVariant.projectSnapshoot

        if (!project.fastdex.useCustomCompile) {
            project.logger.error("==fastdex useCustomCompile=false,disable customJavacTask")
            return
        }

        boolean hasValidCache = fastdexVariant.hasDexCache
        if (!hasValidCache) {
            compileTask.enabled = true
            return
        }

        SourceSetDiffResultSet sourceSetDiffResultSet = projectSnapshoot.diffResultSet
        //java文件是否发生变化
        if (!sourceSetDiffResultSet.isJavaFileChanged()) {
            project.logger.error("==fastdex no java files changed, just ignore")
            compileTask.enabled = false
            return
        }

        //此次变化是否和上次的变化一样
        if (projectSnapshoot.diffResultSet != null
                && projectSnapshoot.oldDiffResultSet != null
                && projectSnapshoot.diffResultSet.equals(projectSnapshoot.oldDiffResultSet)) {
            project.logger.error("==fastdex source set not changed, just ignore")
            compileTask.enabled = false
            return
        }
        Set<PathInfo> addOrModifiedPathInfos = sourceSetDiffResultSet.addOrModifiedPathInfos

        File patchJavaFileDir = new File(FastdexUtils.getBuildDir(project,fastdexVariant.variantName),"custom-combind")
        File patchClassesFileDir = new File(FastdexUtils.getBuildDir(project,fastdexVariant.variantName),"custom-combind-classes")
        FileUtils.deleteDir(patchJavaFileDir)
        FileUtils.ensumeDir(patchClassesFileDir)

        for (PathInfo pathInfo : addOrModifiedPathInfos) {
            project.logger.error("==fastdex changed java file: ${pathInfo.relativePath}")
            FileUtils.copyFileUsingStream(pathInfo.absoluteFile,new File(patchJavaFileDir,pathInfo.relativePath))
        }

        //处理动态生成的java文件
        dealGeneratedSource(addOrModifiedPathInfos,patchJavaFileDir)

        //compile java
        File androidJar = new File("${FastdexUtils.getSdkDirectory(project)}${File.separator}platforms${File.separator}${project.android.getCompileSdkVersion()}${File.separator}android.jar")
        File classpathJar = FastdexUtils.getInjectedJarFile(project,fastdexVariant.variantName)

        def javaCompileTask = fastdexVariant.androidVariant.javaCompile
        //def classpath = project.files(classpathJar.absolutePath) + javaCompileTask.classpath +
        def classpath = project.files(classpathJar.absolutePath)
        def fork = javaCompileTask.options.fork
        def executable = javaCompileTask.options.forkOptions.executable

        //处理retrolambda
        if (project.plugins.hasPlugin("me.tatarka.retrolambda")) {
            fork = true
            //def retrolambda = project.extensions.getByType(RetrolambdaExtension)
            def retrolambda = project.retrolambda
            def rt = "$retrolambda.jdk/jre/lib/rt.jar"
            classpath = classpath + project.files(rt)
            executable = "${retrolambda.tryGetJdk()}/bin/javac"
        }
        project.logger.error("==fastdex androidJar: ${androidJar}")
        project.logger.error("==fastdex classpath: ${classpath.files}")

        //https://ant.apache.org/manual/Tasks/javac.html
        //最好检测下项目根目录的gradle.properties文件,是否有这个配置org.gradle.jvmargs=-Dfile.encoding=UTF-8
        project.ant.javac(
                srcdir: patchJavaFileDir,
                destdir: patchClassesFileDir,
                source: javaCompileTask.sourceCompatibility,
                target: javaCompileTask.targetCompatibility,
                encoding: 'UTF-8',
                bootclasspath: androidJar,
                classpath: joinClasspath(classpath),
                fork: fork,
                executable: executable
        )

        project.logger.error("==fastdex compile success: ${patchClassesFileDir}")
        //覆盖app/build/intermediates/classes内容
        File classesDir = fastdexVariant.androidVariant.getVariantData().getScope().getJavaOutputDir()
        Files.walkFileTree(patchClassesFileDir.toPath(),new SimpleFileVisitor<Path>(){
            @Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = patchClassesFileDir.toPath().relativize(file)
                File destFile = new File(classesDir,relativePath.toString())
                FileUtils.copyFileUsingStream(file.toFile(),destFile)

                project.logger.error("==fastdex apply class to ${destFile}")
                return FileVisitResult.CONTINUE
            }
        })
        compileTask.enabled = false
        //保存对比信息
        fastdexVariant.projectSnapshoot.saveDiffResultSet()
    }

    def dealGeneratedSource(Set<PathInfo> addOrModifiedPathInfos,File patchJavaFileDir) {
        String packageName = fastdexVariant.getApplicationPackageName()
        //copy R.java
        String buildTypeName = fastdexVariant.androidVariant.getBuildType().buildType.getName()
        String packageNamePath = packageName.split("\\.").join(File.separator)
        String rJavaRelativePath = "${packageNamePath}${File.separator}R.java"

        //TODO 路径获取方式暂时先拼接
        File rJavaFile = new File(new File(fastdexVariant.project.buildDir,"generated${File.separator}source${File.separator}r${File.separator}${buildTypeName}${File.separator}"),rJavaRelativePath)
        FileUtils.copyFileUsingStream(rJavaFile,new File(patchJavaFileDir,rJavaRelativePath))

        //copy BuildConfig.java
        String buildConfigJavaRelativePath = "${packageNamePath}${File.separator}BuildConfig.java"
        File buildConfigJavaFile = new File(new File(fastdexVariant.project.buildDir,"generated${File.separator}source${File.separator}buildConfig${File.separator}${buildTypeName}${File.separator}"),buildConfigJavaRelativePath)
        FileUtils.copyFileUsingStream(buildConfigJavaFile,new File(patchJavaFileDir,buildConfigJavaRelativePath))

        //TODO 扫描apt目录
    }

    def joinClasspath(FileCollection collection) {
        StringBuilder sb = new StringBuilder()
        collection.files.each { file ->
            sb.append(file.absolutePath)
            sb.append(":")
        }
        if (sb.toString().endsWith(":")) {
            sb.deleteCharAt(sb.length() - 1)
        }
        return sb
    }
}
