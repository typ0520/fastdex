package fastdex.build.task

import fastdex.build.lib.snapshoot.file.FileNode
import fastdex.build.lib.snapshoot.sourceset.JavaDirectoryDiffResultSet
import fastdex.build.lib.snapshoot.sourceset.JavaDirectorySnapshoot
import fastdex.build.lib.snapshoot.sourceset.JavaFileDiffInfo
import fastdex.build.lib.snapshoot.sourceset.SourceSetDiffResultSet
import fastdex.build.lib.snapshoot.sourceset.SourceSetSnapshoot
import fastdex.build.lib.snapshoot.api.Status
import fastdex.build.util.GradleUtils
import fastdex.build.variant.FastdexVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 恢复对apt目录的扫描
 * Created by tong on 17/10/13.
 */
class FastdexScanAptOutputTask extends DefaultTask {
    FastdexVariant fastdexVariant

    FastdexScanAptOutputTask() {
        group = 'fastdex'
    }

    @TaskAction
    def scan() {
        //如果没有使用自定义的编译任务需要拿老的apt快照与当前的对比
        boolean needDiff = fastdexVariant.hasDexCache && !fastdexVariant.compiledByCustomJavac
        //开启自定义的javac任务正常是不需要在扫描apt目录的，但是如果执行了dex merge会在保存一次当前快照，所以这种情况下要重新加回去
        boolean needScanApt = !fastdexVariant.hasDexCache || needDiff || (fastdexVariant.compiledByCustomJavac && fastdexVariant.willExecDexMerge())

        if (needScanApt) {
            File aptDir = GradleUtils.getAptOutputDir(fastdexVariant.androidVariant)
            JavaDirectorySnapshoot aptDirectorySnapshoot = null

            SourceSetSnapshoot sourceSetSnapshoot = fastdexVariant.projectSnapshoot.sourceSetSnapshoot
            for (JavaDirectorySnapshoot snapshoot : sourceSetSnapshoot.directorySnapshootSet) {
                if (snapshoot.path.equals(aptDir.absolutePath)) {
                    aptDirectorySnapshoot = snapshoot
                    break
                }
            }

            if (aptDirectorySnapshoot == null) {
                //apt
                aptDirectorySnapshoot = new JavaDirectorySnapshoot(aptDir,true)
                aptDirectorySnapshoot.projectPath = fastdexVariant.project.projectDir.absolutePath
                sourceSetSnapshoot.addJavaDirectorySnapshoot(aptDirectorySnapshoot)

                project.logger.error("==fastdex scan apt dir: ${aptDir}")
            }

            if (needDiff) {
                project.logger.error("==fastdex diff apt dir: ${aptDir}")
                SourceSetDiffResultSet diffResultSet = fastdexVariant.projectSnapshoot.diffResultSet
                JavaDirectorySnapshoot oldAptJavaDirectorySnapshoot = fastdexVariant.projectSnapshoot.oldAptJavaDirectorySnapshoot

                if (oldAptJavaDirectorySnapshoot == null) {
                    project.logger.error("==fastdex not find old apt snapshoot")

                    //add
                    JavaDirectoryDiffResultSet resultSet = aptDirectorySnapshoot.createEmptyResultSet()
                    for (FileNode node : aptDirectorySnapshoot.nodes) {
                        resultSet.add(new JavaFileDiffInfo(Status.ADDED,node,null))
                        project.logger.error("==fastdex find new apt file: " + node.uniqueKey)
                    }
                    diffResultSet.mergeJavaDirectoryResultSet(sourceSetSnapshoot.path,resultSet)
                }
                else {
                    //diff
                    JavaDirectoryDiffResultSet resultSet = (JavaDirectoryDiffResultSet) aptDirectorySnapshoot.diff(oldAptJavaDirectorySnapshoot)
                    for (JavaFileDiffInfo diffInfo : resultSet.changedDiffInfos) {
                        if (diffInfo.status == Status.ADDED) {
                            project.logger.error("==fastdex find new apt file: " + diffInfo.uniqueKey)
                        }
                        else if (diffInfo.status == Status.MODIFIED) {
                            project.logger.error("==fastdex find changed apt file: " + diffInfo.uniqueKey)
                        }
                    }
                    diffResultSet.mergeJavaDirectoryResultSet(sourceSetSnapshoot.path,resultSet)
                }
            }
        }
    }
}
