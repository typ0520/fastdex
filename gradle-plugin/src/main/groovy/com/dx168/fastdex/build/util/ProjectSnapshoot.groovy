package com.dx168.fastdex.build.util

import com.dx168.fastdex.build.snapshoot.sourceset.JavaDirectorySnapshoot
import com.dx168.fastdex.build.snapshoot.sourceset.SourceSetDiffResultSet
import com.dx168.fastdex.build.snapshoot.sourceset.SourceSetSnapshoot
import com.dx168.fastdex.build.variant.FastdexVariant
import org.gradle.api.Project

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
        def project = fastdexVariant.project
        sourceSetSnapshoot = new SourceSetSnapshoot(project.projectDir,getProjectSrcDirSet(project))
        handleGeneratedSource(sourceSetSnapshoot)
        handleLibraryDependencies(sourceSetSnapshoot)

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
            saveCurrentSourceSetSnapshoot()
        }
    }

    def handleGeneratedSource(SourceSetSnapshoot snapshoot) {
        List<LibDependency> androidLibDependencies = new ArrayList<>()
        for (LibDependency libDependency : fastdexVariant.libraryDependencies) {
            if (libDependency.androidLibrary) {
                androidLibDependencies.add(libDependency)
            }
        }

        String buildTypeName = fastdexVariant.androidVariant.getBuildType().buildType.getName()
        File rDir = new File(fastdexVariant.project.buildDir,"generated${File.separator}source${File.separator}r${File.separator}${buildTypeName}${File.separator}")
        //r
        JavaDirectorySnapshoot rSnapshoot = new JavaDirectorySnapshoot(rDir,getAllRjavaPath(fastdexVariant.project,androidLibDependencies,buildTypeName))
        snapshoot.addJavaDirectorySnapshoot(rSnapshoot)

        //buildconfig
        List<Project> projectList = new ArrayList<>()
        projectList.add(fastdexVariant.project)
        for (LibDependency libDependency : androidLibDependencies) {
            projectList.add(libDependency.dependencyProject)
        }
        for (int i = 0;i < projectList.size();i++) {
            Project project = projectList.get(i)
            String packageName = GradleUtils.getPackageName(project.android.sourceSets.main.manifest.srcFile.absolutePath)
            String packageNamePath = packageName.split("\\.").join(File.separator)
            //buildconfig
            String buildConfigJavaRelativePath = "${packageNamePath}${File.separator}BuildConfig.java"

            def buildTypeDirName = (i == 0 ? buildTypeName : "release")
            File buildConfigDir = new File(project.buildDir,"generated${File.separator}source${File.separator}buildConfig${File.separator}${buildTypeDirName}${File.separator}")
            File buildConfigJavaFile = new File(buildConfigDir,buildConfigJavaRelativePath)
            if (fastdexVariant.configuration.debug) {
                fastdexVariant.project.logger.error("==fastdex buildConfigJavaFile: ${buildConfigJavaFile}")
            }
            JavaDirectorySnapshoot buildConfigSnapshoot = new JavaDirectorySnapshoot(buildConfigDir,buildConfigJavaFile.absolutePath)
            snapshoot.addJavaDirectorySnapshoot(buildConfigSnapshoot)
        }
    }

    def handleLibraryDependencies(SourceSetSnapshoot snapshoot) {
        for (LibDependency libDependency : fastdexVariant.libraryDependencies) {
            Set<File> srcDirSet = getProjectSrcDirSet(libDependency.dependencyProject)

            for (File file : srcDirSet) {
                snapshoot.addJavaDirectorySnapshoot(new JavaDirectorySnapshoot(file))
            }
        }
    }

    def getAllRjavaPath(Project appProject,List<LibDependency> androidLibDependencies,String buildTypeName) {
        File rDir = new File(appProject.buildDir,"generated${File.separator}source${File.separator}r${File.separator}${buildTypeName}${File.separator}")
        List<File> fileList = new ArrayList<>()
        for (LibDependency libDependency : androidLibDependencies) {
            String packageName = GradleUtils.getPackageName(libDependency.dependencyProject.android.sourceSets.main.manifest.srcFile.absolutePath)
            String packageNamePath = packageName.split("\\.").join(File.separator)

            String rjavaRelativePath = "${packageNamePath}${File.separator}R.java"
            File rjavaFile = new File(rDir,rjavaRelativePath)
            fileList.add(rjavaFile)

            if (fastdexVariant.configuration.debug) {
                fastdexVariant.project.logger.error("==fastdex rjavaFile: ${rjavaFile}")
            }
        }
        return fileList
    }

    def getProjectSrcDirSet(Project project) {
        def srcDirs = null
        if (project.plugins.hasPlugin("com.android.application") || project.plugins.hasPlugin("com.android.library")) {
            srcDirs = project.android.sourceSets.main.java.srcDirs
        }
        else {
            srcDirs = project.sourceSets.main.java.srcDirs
        }
        if (fastdexVariant.configuration.debug) {
            project.logger.error("==fastdex: ${project} ${srcDirs}")
        }
        Set<File> srcDirSet = new HashSet<>()
        if (srcDirs != null) {
            for (java.lang.Object src : srcDirs) {
                if (src instanceof File) {
                    srcDirSet.add(src)
                }
                else if (src instanceof String) {
                    srcDirSet.add(new File(src))
                }
            }
        }
        return srcDirSet
    }

    def saveSourceSetSnapshoot(SourceSetSnapshoot snapshoot) {
        snapshoot.serializeTo(new FileOutputStream(FastdexUtils.getSourceSetSnapshootFile(fastdexVariant.project,fastdexVariant.variantName)))
    }

    def saveCurrentSourceSetSnapshoot() {
        saveSourceSetSnapshoot(sourceSetSnapshoot)
    }

    def saveDiffResultSet() {
        if (diffResultSet != null && !diffResultSet.changedJavaFileDiffInfos.empty) {
            File diffResultSetFile = FastdexUtils.getDiffResultSetFile(fastdexVariant.project,fastdexVariant.variantName)
            //全量打包后首次java文件发生变化
            diffResultSet.serializeTo(new FileOutputStream(diffResultSetFile))
        }
    }

    def deleteLastDiffResultSet() {
        File diffResultSetFile = FastdexUtils.getDiffResultSetFile(fastdexVariant.project,fastdexVariant.variantName)
        FileUtils.deleteFile(diffResultSetFile)
    }
}
