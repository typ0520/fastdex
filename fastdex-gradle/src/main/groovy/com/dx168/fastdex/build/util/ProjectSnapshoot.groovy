package com.dx168.fastdex.build.util

import fastdex.build.lib.snapshoot.sourceset.JavaDirectorySnapshoot
import fastdex.build.lib.snapshoot.sourceset.SourceSetDiffResultSet
import fastdex.build.lib.snapshoot.sourceset.SourceSetSnapshoot
import fastdex.build.lib.snapshoot.string.StringNode
import fastdex.build.lib.snapshoot.string.StringSnapshoot
import com.dx168.fastdex.build.variant.FastdexVariant
import org.gradle.api.Project
import fastdex.common.utils.FileUtils

/**
 * Created by tong on 17/3/31.
 */
public class ProjectSnapshoot {
    FastdexVariant fastdexVariant
    SourceSetSnapshoot sourceSetSnapshoot
    SourceSetSnapshoot oldSourceSetSnapshoot
    SourceSetDiffResultSet diffResultSet
    SourceSetDiffResultSet oldDiffResultSet
    StringSnapshoot dependenciesSnapshoot
    StringSnapshoot oldDependenciesSnapshoot

    ProjectSnapshoot(FastdexVariant fastdexVariant) {
        this.fastdexVariant = fastdexVariant
    }

    def loadSnapshoot() {
        if (!fastdexVariant.hasDexCache) {
            return
        }
        def project = fastdexVariant.project
        //load old sourceSet
        File sourceSetSnapshootFile = FastdexUtils.getSourceSetSnapshootFile(project,fastdexVariant.variantName)
        oldSourceSetSnapshoot = SourceSetSnapshoot.load(sourceSetSnapshootFile,SourceSetSnapshoot.class)

        File dependenciesListFile = FastdexUtils.getCachedDependListFile(project,fastdexVariant.variantName)
        oldDependenciesSnapshoot = StringSnapshoot.load(dependenciesListFile,StringSnapshoot.class)

        String oldProjectPath = fastdexVariant.metaInfo.projectPath
        String curProjectPath = project.projectDir.absolutePath

        String oldRootProjectPath = fastdexVariant.metaInfo.rootProjectPath
        String curRootProjectPath = project.rootProject.projectDir.absolutePath
        boolean isRootProjectDirChanged = fastdexVariant.metaInfo.isRootProjectDirChanged(curRootProjectPath)
        if (isRootProjectDirChanged) {
            //已存在构建缓存的情况下,如果移动了项目目录要把缓存中的老的路径全部替换掉
            applyNewProjectDir(oldSourceSetSnapshoot,oldRootProjectPath,curRootProjectPath,curProjectPath)
            if (oldSourceSetSnapshoot.lastDiffResult != null) {
                oldSourceSetSnapshoot.lastDiffResult = null
            }
            //save
            saveSourceSetSnapshoot(oldSourceSetSnapshoot)

            for (StringNode node : oldDependenciesSnapshoot.nodes) {
                node.string = replacePath(node.string,oldRootProjectPath,curRootProjectPath)
            }
            saveDependenciesSnapshoot(oldDependenciesSnapshoot)

            fastdexVariant.metaInfo.projectPath = curProjectPath
            fastdexVariant.metaInfo.rootProjectPath = curRootProjectPath
            fastdexVariant.saveMetaInfo()
            project.logger.error("==fastdex restore cache, project path changed old: ${oldProjectPath} now: ${curProjectPath}")
        }
    }

    def applyNewProjectDir(SourceSetSnapshoot sourceSnapshoot,String oldRootProjectPath,String curRootProjectPath,String curProjectPath) {
        sourceSnapshoot.path = curProjectPath
        for (StringNode node : sourceSnapshoot.nodes) {
            node.setString(replacePath(node.getString(),oldRootProjectPath,curRootProjectPath))
        }
        for (JavaDirectorySnapshoot snapshoot : sourceSnapshoot.directorySnapshootSet) {
            snapshoot.path = replacePath(snapshoot.path,oldRootProjectPath,curRootProjectPath)
            snapshoot.projectPath = replacePath(snapshoot.projectPath,oldRootProjectPath,curRootProjectPath)
        }
    }

    def replacePath(String path,String s,String s1) {
        if (path.startsWith(s)) {
            path = path.substring(s.length());
            path = s1 + path;
        }
        return path;
    }

    def prepareEnv() {
        def project = fastdexVariant.project
        sourceSetSnapshoot = new SourceSetSnapshoot(project.projectDir,getProjectSrcDirSet(project))
        handleGeneratedSource(sourceSetSnapshoot)
        handleLibraryDependencies(sourceSetSnapshoot)

        if (fastdexVariant.hasDexCache) {
            diffResultSet = sourceSetSnapshoot.diff(oldSourceSetSnapshoot)
            if (!fastdexVariant.firstPatchBuild) {
                File diffResultSetFile = FastdexUtils.getDiffResultSetFile(project,fastdexVariant.variantName)
                oldDiffResultSet = SourceSetDiffResultSet.load(diffResultSetFile,SourceSetDiffResultSet.class)
            }
        }
    }

    /**
     * 把自动生成的代码添加到源码快照中(R.java、buildConfig.java)
     * @param snapshoot
     */
    def handleGeneratedSource(SourceSetSnapshoot snapshoot) {
        List<LibDependency> androidLibDependencies = new ArrayList<>()
        for (LibDependency libDependency : fastdexVariant.libraryDependencies) {
            if (libDependency.androidLibrary) {
                androidLibDependencies.add(libDependency)
            }
        }

        //TODO change api
        //File rDir = new File(fastdexVariant.project.buildDir,"generated${File.separator}source${File.separator}r${File.separator}${fastdexVariant.androidVariant.dirName}${File.separator}")
        File rDir = fastdexVariant.androidVariant.getVariantData().getScope().getRClassSourceOutputDir()
        //r
        JavaDirectorySnapshoot rSnapshoot = new JavaDirectorySnapshoot(rDir,getAllRjavaPath(fastdexVariant.project,androidLibDependencies))
        rSnapshoot.projectPath = fastdexVariant.project.projectDir.absolutePath
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
        //libraryVariantdirName Constants.DEFAULT_LIBRARY_VARIANT_DIR_NAME
        def libraryVariantdirName = Constants.DEFAULT_LIBRARY_VARIANT_DIR_NAME
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
            //libraryVariantdirName Constants.DEFAULT_LIBRARY_VARIANT_DIR_NAME
            libraryVariantdirName = dirName.substring(0,dirName.length() - buildTypeName.length())
            libraryVariantdirName = "${libraryVariantdirName}${Constants.DEFAULT_LIBRARY_VARIANT_DIR_NAME}"

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
                //TODO change api
                buildConfigDir = fastdexVariant.androidVariant.getVariantData().getScope().getBuildConfigSourceOutputDir()
                //buildConfigDir = new File(project.buildDir,"generated${File.separator}source${File.separator}buildConfig${File.separator}${fastdexVariant.androidVariant.dirName}${File.separator}")
            }
            else {
                buildConfigDir = new File(project.buildDir,"generated${File.separator}source${File.separator}buildConfig${File.separator}${libraryVariantdirName}${File.separator}")
            }
            File buildConfigJavaFile = new File(buildConfigDir,buildConfigJavaRelativePath)
            if (fastdexVariant.configuration.debug) {
                fastdexVariant.project.logger.error("==fastdex buildConfigJavaFile: ${buildConfigJavaFile}")
            }
            JavaDirectorySnapshoot buildConfigSnapshoot = new JavaDirectorySnapshoot(buildConfigDir,buildConfigJavaFile.absolutePath)
            buildConfigSnapshoot.projectPath = project.projectDir.absolutePath
            snapshoot.addJavaDirectorySnapshoot(buildConfigSnapshoot)
        }
    }

    /**
     * 往源码快照里添加依赖的工程源码路径
     * @param snapshoot
     */
    def handleLibraryDependencies(SourceSetSnapshoot snapshoot) {
        for (LibDependency libDependency : fastdexVariant.libraryDependencies) {
            Set<File> srcDirSet = getProjectSrcDirSet(libDependency.dependencyProject)

            for (File file : srcDirSet) {
                JavaDirectorySnapshoot javaDirectorySnapshoot = new JavaDirectorySnapshoot(file)
                javaDirectorySnapshoot.projectPath = libDependency.dependencyProject.projectDir.absolutePath
                snapshoot.addJavaDirectorySnapshoot(javaDirectorySnapshoot)
            }
        }
    }

    /**
     * 获取application工程自身和依赖的aar工程的所有R文件相对路径
     * @param appProject
     * @param androidLibDependencies
     * @return
     */
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

    /**
     * 获取工程对应的所有源码目录
     * @param project
     * @return
     */
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

    /**
     * 保存源码快照信息
     * @param snapshoot
     * @return
     */
    def saveSourceSetSnapshoot(SourceSetSnapshoot snapshoot) {
        snapshoot.serializeTo(new FileOutputStream(FastdexUtils.getSourceSetSnapshootFile(fastdexVariant.project,fastdexVariant.variantName)))
    }

    /**
     * 保存当前的源码快照信息
     * @return
     */
    def saveCurrentSourceSetSnapshoot() {
        saveSourceSetSnapshoot(sourceSetSnapshoot)
    }

    /**
     * 保存源码对比结果
     * @return
     */
    def saveDiffResultSet() {
        if (diffResultSet != null && !diffResultSet.changedJavaFileDiffInfos.empty) {
            File diffResultSetFile = FastdexUtils.getDiffResultSetFile(fastdexVariant.project,fastdexVariant.variantName)
            //全量打包后首次java文件发生变化
            diffResultSet.serializeTo(new FileOutputStream(diffResultSetFile))
        }
    }

    /**
     * 删除源码对比结果
     * @return
     */
    def deleteLastDiffResultSet() {
        File diffResultSetFile = FastdexUtils.getDiffResultSetFile(fastdexVariant.project,fastdexVariant.variantName)
        FileUtils.deleteFile(diffResultSetFile)
    }

    /**
     * 依赖列表是否发生变化
     * @return
     */
    def isDependenciesChanged() {
        if (dependenciesSnapshoot == null) {
            dependenciesSnapshoot = new StringSnapshoot(GradleUtils.getCurrentDependList(fastdexVariant.project,fastdexVariant.androidVariant))
        }

        if (oldDependenciesSnapshoot == null) {
            File dependenciesListFile = FastdexUtils.getCachedDependListFile(fastdexVariant.project,fastdexVariant.variantName)
            oldDependenciesSnapshoot = StringSnapshoot.load(dependenciesListFile,StringSnapshoot.class)
        }
        return !dependenciesSnapshoot.diff(oldDependenciesSnapshoot).getAllChangedDiffInfos().isEmpty()
    }

    /**
     * 保存全量打包时的依赖列表
     */
    def saveDependenciesSnapshoot() {
        if (dependenciesSnapshoot == null) {
            dependenciesSnapshoot = new StringSnapshoot(GradleUtils.getCurrentDependList(fastdexVariant.project,fastdexVariant.androidVariant))
        }
        saveDependenciesSnapshoot(dependenciesSnapshoot)
    }

    /**
     * 保存依赖列表
     * @param snapshoot
     * @return
     */
    def saveDependenciesSnapshoot(StringSnapshoot snapshoot) {
        File dependenciesListFile = FastdexUtils.getCachedDependListFile(fastdexVariant.project,fastdexVariant.variantName)

        StringSnapshoot stringSnapshoot = new StringSnapshoot()
        stringSnapshoot.nodes = snapshoot.nodes
        stringSnapshoot.serializeTo(new FileOutputStream(dependenciesListFile))
    }

    def onDexGenerateSuccess(boolean nornalBuild,boolean dexMerge) {
        if (nornalBuild) {
            //save sourceSet
            saveCurrentSourceSetSnapshoot()
            //save dependencies
            saveDependenciesSnapshoot()
        }
        else {
            if (dexMerge) {
                //save snapshoot and diffinfo
                saveCurrentSourceSetSnapshoot()
                deleteLastDiffResultSet()
            }
        }
    }
}
