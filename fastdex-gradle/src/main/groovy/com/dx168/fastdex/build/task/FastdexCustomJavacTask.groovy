package com.dx168.fastdex.build.task

import fastdex.build.lib.snapshoot.sourceset.PathInfo
import fastdex.build.lib.snapshoot.sourceset.SourceSetDiffResultSet
import com.dx168.fastdex.build.util.FastdexUtils
import fastdex.common.utils.FileUtils
import com.dx168.fastdex.build.variant.FastdexVariant
import org.gradle.api.DefaultTask
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

    FastdexCustomJavacTask() {
        group = 'fastdex'
    }

    @TaskAction
    void compile() {
        def compileTask = fastdexVariant.androidVariant.javaCompile
        compileTask.enabled = true

        def project = fastdexVariant.project
        def projectSnapshoot = fastdexVariant.projectSnapshoot

        File classesDir = fastdexVariant.androidVariant.getVariantData().getScope().getJavaOutputDir()
        if (!FileUtils.dirExists(classesDir.absolutePath)) {
            return
        }

        if (!fastdexVariant.configuration.useCustomCompile) {
            return
        }

        if (!fastdexVariant.hasDexCache) {
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
            project.logger.error("==fastdex java files not changed, just ignore")
            compileTask.enabled = false
            return
        }
        Set<PathInfo> addOrModifiedPathInfos = sourceSetDiffResultSet.addOrModifiedPathInfos

        File patchJavaFileDir = new File(FastdexUtils.getWorkDir(project,fastdexVariant.variantName),"custom-combind")
        File patchClassesFileDir = new File(FastdexUtils.getWorkDir(project,fastdexVariant.variantName),"custom-combind-classes")
        FileUtils.deleteDir(patchJavaFileDir)
        FileUtils.ensumeDir(patchClassesFileDir)

        for (PathInfo pathInfo : addOrModifiedPathInfos) {
            project.logger.error("==fastdex changed java file: ${pathInfo.relativePath}")
            FileUtils.copyFileUsingStream(pathInfo.absoluteFile,new File(patchJavaFileDir,pathInfo.relativePath))
        }

        //处理动态生成的java文件
        handleApt(addOrModifiedPathInfos,patchJavaFileDir)

        //compile java
        File androidJar = new File("${FastdexUtils.getSdkDirectory(project)}${File.separator}platforms${File.separator}${project.android.getCompileSdkVersion()}${File.separator}android.jar")
        File classpathJar = FastdexUtils.getInjectedJarFile(project,fastdexVariant.variantName)

        //def classpath = project.files(classpathJar.absolutePath) + compileTask.classpath +
        def classpath = project.files(classpathJar.absolutePath)
        def fork = compileTask.options.fork
        def executable = compileTask.options.forkOptions.executable

        project.logger.error("==fastdex executable ${executable}")
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
                source: compileTask.sourceCompatibility,
                target: compileTask.targetCompatibility,
                encoding: 'UTF-8',
                bootclasspath: androidJar,
                classpath: joinClasspath(classpath),
                fork: fork,
                executable: executable
        )

        project.logger.error("==fastdex compile success: ${patchClassesFileDir}")

        //覆盖app/build/intermediates/classes内容
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

    def handleApt(Set<PathInfo> addOrModifiedPathInfos, File patchJavaFileDir) {
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
