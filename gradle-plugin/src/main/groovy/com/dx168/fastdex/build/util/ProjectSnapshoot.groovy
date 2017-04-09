package com.dx168.fastdex.build.util

import com.dx168.fastdex.build.snapshoot.sourceset.SourceSetDiffResultSet
import com.dx168.fastdex.build.snapshoot.sourceset.SourceSetSnapshoot
import com.dx168.fastdex.build.variant.FastdexVariant

/**
 * Created by tong on 17/3/31.
 */
public class ProjectSnapshoot {
    FastdexVariant fastdexVariant
    SourceSetSnapshoot sourceSetSnapshoot
    SourceSetDiffResultSet diffResultSet
    SourceSetDiffResultSet oldDiffResultSet

    ProjectSnapshoot(FastdexVariant fastdexVariant) {
        this.fastdexVariant = fastdexVariant
    }

    def prepareEnv() {
        if (sourceSetSnapshoot != null) {
            return
        }

        def project = fastdexVariant.project
        def srcDirs = project.android.sourceSets.main.java.srcDirs
        sourceSetSnapshoot = new SourceSetSnapshoot(project.projectDir,srcDirs)

        if (fastdexVariant.hasDexCache) {
            //load old sourceSet
            File sourceSetSnapshootFile = FastdexUtils.getSourceSetSnapshootFile(project,fastdexVariant.variantName)
            SourceSetSnapshoot oldSourceSetSnapshoot = SourceSetSnapshoot.load(sourceSetSnapshootFile,SourceSetSnapshoot.class)

            String oldProjectDir = oldSourceSetSnapshoot.path
            boolean isProjectDirChanged = oldSourceSetSnapshoot.ensumeProjectDir(project.projectDir)
            if (isProjectDirChanged) {
                project.logger.error("==fastdex project-dir changed old: ${oldProjectDir} now: ${project.projectDir}")
                //save
                saveSourceSetSnapshoot(oldSourceSetSnapshoot)
            }

            diffResultSet = sourceSetSnapshoot.diff(oldSourceSetSnapshoot)
            if (!fastdexVariant.firstPatchBuild) {
                File diffResultSetFile = FastdexUtils.getDiffResultSetFile(project,fastdexVariant.variantName)
                oldDiffResultSet = SourceSetDiffResultSet.load(diffResultSetFile,SourceSetDiffResultSet.class)
            }
        }
        else {
            //save
            saveSourceSetSnapshoot(sourceSetSnapshoot)
        }
    }

    def saveSourceSetSnapshoot(SourceSetSnapshoot snapshoot) {
        snapshoot.serializeTo(new FileOutputStream(FastdexUtils.getSourceSetSnapshootFile(fastdexVariant.project,fastdexVariant.variantName)))
    }

    def saveDiffResultSet() {
        if (diffResultSet != null && !diffResultSet.changedJavaFileDiffInfos.empty) {
            File diffResultSetFile = FastdexUtils.getDiffResultSetFile(fastdexVariant.project,fastdexVariant.variantName)
            //全量打包后首次java文件发生变化
            diffResultSet.serializeTo(new FileOutputStream(diffResultSetFile))
        }
    }
}
