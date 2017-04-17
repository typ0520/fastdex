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

        File rDir = new File(fastdexVariant.project.buildDir,"generated${File.separator}source${File.separator}r${File.separator}${fastdexVariant.androidVariant.dirName}${File.separator}")
        //r
        JavaDirectorySnapshoot rSnapshoot = new JavaDirectorySnapshoot(rDir,getAllRjavaPath(fastdexVariant.project,androidLibDependencies))
        snapshoot.addJavaDirectorySnapshoot(rSnapshoot)

        //buildconfig
        List<Project> projectList = new ArrayList<>()
        projectList.add(fastdexVariant.project)
        for (LibDependency libDependency : androidLibDependencies) {
            projectList.add(libDependency.dependencyProject)
        }

        String buildTypeName = fastdexVariant.androidVariant.getBuildType().buildType.getName()
        String dirName = fastdexVariant.androidVariant.dirName
        //buildTypeName         "debug"
        //dirName               "debug"
        //libraryVariantdirName Constant.DEFAULT_LIBRARY_VARIANT_DIR_NAME
        def libraryVariantdirName = Constant.DEFAULT_LIBRARY_VARIANT_DIR_NAME
        /**
         * fix-issue32 https://github.com/typ0520/fastdex/issues/32
         * 正常的buildConfig目录
         * /Users/zhengmj/Desktop/TjrTaojinRoad/common/build/generated/source/buildConfig/release
         *
         * issue32对应的buildConfig目录
         * /Users/zhengmj/Desktop/TjrTaojinRoad/common/build/generated/source/buildConfig/taojinroad/release
         */
        if (!dirName.equals(buildTypeName)) {
            //buildTypeName         "debug"
            //dirName               "xxxx/debug"
            //libraryVariantdirName Constant.DEFAULT_LIBRARY_VARIANT_DIR_NAME
            libraryVariantdirName = dirName.substring(0,dirName.length() - buildTypeName.length())
            libraryVariantdirName = "${libraryVariantdirName}${Constant.DEFAULT_LIBRARY_VARIANT_DIR_NAME}"

            if (libraryVariantdirName.startsWith(File.separator)) {
                libraryVariantdirName = libraryVariantdirName.substring(1)
            }
            if (libraryVariantdirName.endsWith(File.separator)) {
                libraryVariantdirName = libraryVariantdirName.substring(0,libraryVariantdirName.length() - 1)
            }
        }
        for (int i = 0;i < projectList.size();i++) {
            Project project = projectList.get(i)
            String packageName = GradleUtils.getPackageName(project.android.sourceSets.main.manifest.srcFile.absolutePath)
            String packageNamePath = packageName.split("\\.").join(File.separator)
            //buildconfig
            String buildConfigJavaRelativePath = "${packageNamePath}${File.separator}BuildConfig.java"
            File buildConfigDir = null
            if (i == 0) {
                buildConfigDir = new File(project.buildDir,"generated${File.separator}source${File.separator}buildConfig${File.separator}${fastdexVariant.androidVariant.dirName}${File.separator}")
            }
            else {
                buildConfigDir = new File(project.buildDir,"generated${File.separator}source${File.separator}buildConfig${File.separator}${libraryVariantdirName}${File.separator}")
            }
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

    def getAllRjavaPath(Project appProject,List<LibDependency> androidLibDependencies) {
        File rDir = new File(appProject.buildDir,"generated${File.separator}source${File.separator}r${File.separator}${fastdexVariant.androidVariant.dirName}${File.separator}")
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
