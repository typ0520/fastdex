package fastdex.build.util

import fastdex.build.lib.snapshoot.api.DiffResultSet
import fastdex.build.lib.snapshoot.file.FileDiffInfo
import fastdex.build.lib.snapshoot.res.AndManifestDirectorySnapshoot
import fastdex.build.lib.snapshoot.sourceset.JavaDirectorySnapshoot
import fastdex.build.lib.snapshoot.sourceset.SourceSetDiffResultSet
import fastdex.build.lib.snapshoot.sourceset.SourceSetSnapshoot
import fastdex.build.lib.snapshoot.string.StringSnapshoot
import fastdex.build.variant.FastdexVariant
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
    AndManifestDirectorySnapshoot andManifestDirectorySnapshoot
    AndManifestDirectorySnapshoot oldAndManifestDirectorySnapshoot

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

        //load dependencies
        File dependenciesListFile = FastdexUtils.getCachedDependListFile(project,fastdexVariant.variantName)
        oldDependenciesSnapshoot = StringSnapshoot.load(dependenciesListFile,StringSnapshoot.class)

        //load manifest
        File androidManifestStatFile = FastdexUtils.getAndroidManifestStatFile(project,fastdexVariant.variantName)
        oldAndManifestDirectorySnapshoot = AndManifestDirectorySnapshoot.load(androidManifestStatFile,AndManifestDirectorySnapshoot.class)


        //TODO 暂时移除目录移动后的容错处理
//        String oldProjectPath = fastdexVariant.metaInfo.projectPath
//        String curProjectPath = project.projectDir.absolutePath
//
//        String oldRootProjectPath = fastdexVariant.metaInfo.rootProjectPath
//        String curRootProjectPath = project.rootProject.projectDir.absolutePath
//
//        boolean isRootProjectDirChanged = fastdexVariant.metaInfo.isRootProjectDirChanged(curRootProjectPath)
//
//        if (isRootProjectDirChanged) {
//            //已存在构建缓存的情况下,如果移动了项目目录要把缓存中的老的路径全部替换掉
//            applyNewProjectDir(oldSourceSetSnapshoot,oldRootProjectPath,curRootProjectPath,curProjectPath)
//            if (oldSourceSetSnapshoot.lastDiffResult != null) {
//                oldSourceSetSnapshoot.lastDiffResult = null
//            }
//            //save
//            saveSourceSetSnapshoot(oldSourceSetSnapshoot)
//
//            for (StringNode node : oldDependenciesSnapshoot.nodes) {
//                node.string = replacePath(node.string,oldRootProjectPath,curRootProjectPath)
//            }
//            saveDependenciesSnapshoot(oldDependenciesSnapshoot)
//
//            fastdexVariant.metaInfo.projectPath = curProjectPath
//            fastdexVariant.metaInfo.rootProjectPath = curRootProjectPath
//            fastdexVariant.saveMetaInfo()
//            project.logger.error("==fastdex restore cache, project path changed old: ${oldProjectPath} now: ${curProjectPath}")
//        }
    }

//    def applyNewProjectDir(SourceSetSnapshoot sourceSnapshoot,String oldRootProjectPath,String curRootProjectPath,String curProjectPath) {
//        sourceSnapshoot.path = curProjectPath
//        for (StringNode node : sourceSnapshoot.nodes) {
//            node.setString(replacePath(node.getString(),oldRootProjectPath,curRootProjectPath))
//        }
//        for (JavaDirectorySnapshoot snapshoot : sourceSnapshoot.directorySnapshootSet) {
//            snapshoot.path = replacePath(snapshoot.path,oldRootProjectPath,curRootProjectPath)
//            snapshoot.projectPath = replacePath(snapshoot.projectPath,oldRootProjectPath,curRootProjectPath)
//        }
//    }

//    def replacePath(String path,String s,String s1) {
//        if (path.startsWith(s)) {
//            path = path.substring(s.length());
//            path = s1 + path;
//        }
//        return path;
//    }

    def prepareEnv() {
        def project = fastdexVariant.project

        sourceSetSnapshoot = new SourceSetSnapshoot(project.projectDir,getProjectSrcDirSet(project))
        handleGeneratedSource(sourceSetSnapshoot)
        handleLibraryDependencies(sourceSetSnapshoot)

        andManifestDirectorySnapshoot = new AndManifestDirectorySnapshoot()

        def list = getProjectManifestFiles(fastdexVariant.project)
        list.each {
            andManifestDirectorySnapshoot.addFile(it)
        }

        for (LibDependency libDependency : fastdexVariant.libraryDependencies) {
            if (libDependency.androidLibrary) {
                File file = libDependency.dependencyProject.android.sourceSets.main.manifest.srcFile
                andManifestDirectorySnapshoot.addFile(file)
            }
        }

        if (fastdexVariant.hasDexCache) {
            diffResultSet = sourceSetSnapshoot.diff(oldSourceSetSnapshoot)
            if (!fastdexVariant.firstPatchBuild) {
                File diffResultSetFile = FastdexUtils.getDiffResultSetFile(project,fastdexVariant.variantName)
                oldDiffResultSet = SourceSetDiffResultSet.load(diffResultSetFile,SourceSetDiffResultSet.class)
            }

            DiffResultSet<FileDiffInfo> diffResultSet = andManifestDirectorySnapshoot.diff(oldAndManifestDirectorySnapshoot)
            if (diffResultSet != null && diffResultSet.changedDiffInfos.size() > 0) {
                //如果manifest文件发生变化，改变buildMillis的值，这样走到免安装时就会重新安装(如果增加了四大组件必须重新安装app)
                fastdexVariant.metaInfo.buildMillis = System.currentTimeMillis()
                fastdexVariant.saveMetaInfo()

                File androidManifestStatFile = FastdexUtils.getAndroidManifestStatFile(project,fastdexVariant.variantName)
                andManifestDirectorySnapshoot.serializeTo(new FileOutputStream(androidManifestStatFile))

                fastdexVariant.fastdexInstantRun.onManifestChanged()
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

        List<Project> projectList = new ArrayList<>()
        projectList.add(fastdexVariant.project)
        for (LibDependency libDependency : androidLibDependencies) {
            projectList.add(libDependency.dependencyProject)
        }

        def libraryVariantdirName = Constants.DEFAULT_LIBRARY_VARIANT_DIR_NAME

        //dataBinding
        if (FastdexUtils.isDataBindingEnabled(fastdexVariant.project)) {
            File dataBindingDir = fastdexVariant.androidVariant.getVariantData().getScope().getClassOutputForDataBinding()
            if (fastdexVariant.configuration.debug) {
                fastdexVariant.project.logger.error("==fastdex dataBinding: ${dataBindingDir}")
            }
            JavaDirectorySnapshoot dataBindingDirectorySnapshoot = new JavaDirectorySnapshoot(dataBindingDir,true)
            dataBindingDirectorySnapshoot.projectPath = fastdexVariant.project.projectDir.absolutePath
            snapshoot.addJavaDirectorySnapshoot(dataBindingDirectorySnapshoot)
        }

        for (int i = 0;i < projectList.size();i++) {
            Project project = projectList.get(i)
            String packageName = GradleUtils.getPackageName(project.android.sourceSets.main.manifest.srcFile.absolutePath)
            String packageNamePath = packageName.split("\\.").join(File.separator)
            //buildconfig
            String buildConfigJavaRelativePath = "${packageNamePath}${File.separator}BuildConfig.java"
            String rJavaRelativePath = "${packageNamePath}${File.separator}R.java"

            File buildConfigDir = null
            File rDir = null
            File rsDir = null
            File aidlDir = null
            File aptDir = null
            if (i == 0) {
                buildConfigDir = fastdexVariant.androidVariant.getVariantData().getScope().getBuildConfigSourceOutputDir()
                rDir = fastdexVariant.androidVariant.getVariantData().getScope().getRClassSourceOutputDir()
                rsDir = fastdexVariant.androidVariant.getVariantData().getScope().getRenderscriptSourceOutputDir()
                aidlDir = fastdexVariant.androidVariant.getVariantData().getScope().getAidlSourceOutputDir()
                if (GradleUtils.ANDROID_GRADLE_PLUGIN_VERSION.compareTo("2.2") >= 0) {
                    //2.2.0以后才有getAnnotationProcessorOutputDir()这个api
                    aptDir = fastdexVariant.androidVariant.getVariantData().getScope().getAnnotationProcessorOutputDir()
                }
                else {
                    aptDir = new File(project.buildDir,"generated${File.separator}source${File.separator}apt${File.separator}${libraryVariantdirName}${File.separator}")
                }
            }
            else {
                buildConfigDir = new File(project.buildDir,"generated${File.separator}source${File.separator}buildConfig${File.separator}${libraryVariantdirName}${File.separator}")
                rDir = new File(project.buildDir,"generated${File.separator}source${File.separator}r${File.separator}${libraryVariantdirName}${File.separator}")
                rsDir = new File(project.buildDir,"generated${File.separator}source${File.separator}rs${File.separator}${libraryVariantdirName}${File.separator}")
                aidlDir = new File(project.buildDir,"generated${File.separator}source${File.separator}aidl${File.separator}${libraryVariantdirName}${File.separator}")
                aptDir = new File(project.buildDir,"generated${File.separator}source${File.separator}apt${File.separator}${libraryVariantdirName}${File.separator}")
            }
            File buildConfigJavaFile = new File(buildConfigDir,buildConfigJavaRelativePath)
            if (fastdexVariant.configuration.debug) {
                fastdexVariant.project.logger.error("==fastdex buildConfigJavaFile: ${buildConfigJavaFile}")
            }
            JavaDirectorySnapshoot buildConfigSnapshoot = new JavaDirectorySnapshoot(buildConfigDir,true,buildConfigJavaFile.absolutePath)
            buildConfigSnapshoot.projectPath = project.projectDir.absolutePath
            snapshoot.addJavaDirectorySnapshoot(buildConfigSnapshoot)

            File rJavaFile = new File(rDir,rJavaRelativePath)
            if (fastdexVariant.configuration.debug) {
                fastdexVariant.project.logger.error("==fastdex rJavaFile: ${rJavaFile}")
            }
            JavaDirectorySnapshoot rSnapshoot = new JavaDirectorySnapshoot(rDir,true,rJavaFile.absolutePath)
            rSnapshoot.projectPath = project.projectDir.absolutePath
            snapshoot.addJavaDirectorySnapshoot(rSnapshoot)

            //rs
            JavaDirectorySnapshoot rsDirectorySnapshoot = new JavaDirectorySnapshoot(rsDir)
            rsDirectorySnapshoot.projectPath = project.projectDir.absolutePath
            snapshoot.addJavaDirectorySnapshoot(rsDirectorySnapshoot)

            //aidl
            JavaDirectorySnapshoot aidlDirectorySnapshoot = new JavaDirectorySnapshoot(aidlDir)
            aidlDirectorySnapshoot.projectPath = project.projectDir.absolutePath
            snapshoot.addJavaDirectorySnapshoot(aidlDirectorySnapshoot)

            if (fastdexVariant.configuration.traceApt || FastdexUtils.isDataBindingEnabled(project)) {
                //apt
                JavaDirectorySnapshoot aptDirectorySnapshoot = new JavaDirectorySnapshoot(aptDir,new Exclude$SourceSuffixFilter(),true)
                aptDirectorySnapshoot.projectPath = project.projectDir.absolutePath
                snapshoot.addJavaDirectorySnapshoot(aptDirectorySnapshoot)
            }
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
     * 获取工程对应的所有源码目录
     * @param project
     * @return
     */
    def getProjectSrcDirSet(Project project) {
        def srcDirs = new LinkedHashSet()
        if (project.hasProperty("android") && project.android.hasProperty("sourceSets")) {
            //srcDirs = project.android.sourceSets.main.java.srcDirs
            srcDirs.addAll(FastdexUtils.getSrcDirs(project,"main"))

            String buildTypeName = fastdexVariant.androidVariant.getBuildType().buildType.getName()
            String flavorName = fastdexVariant.androidVariant.flavorName

            if (buildTypeName && flavorName) {
                srcDirs.addAll(FastdexUtils.getSrcDirs(project,flavorName + buildTypeName.capitalize() as String))
            }

            if (buildTypeName) {
                srcDirs.addAll(FastdexUtils.getSrcDirs(project,buildTypeName))
            }

            if (flavorName) {
                srcDirs.addAll(FastdexUtils.getSrcDirs(project,flavorName))
            }
        }
        else if (project.plugins.hasPlugin("java") && project.hasProperty("sourceSets")) {
            srcDirs.addAll(project.sourceSets.main.java.srcDirs.asList())
        }
        if (fastdexVariant.configuration.debug) {
            project.logger.error("==fastdex: sourceSets ${srcDirs}")
        }
        Set<File> srcDirSet = new LinkedHashSet<>()
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

    def getProjectManifestFiles(Project project) {
        def manifestFiles = new LinkedHashSet()

        if (project.hasProperty("android") && project.android.hasProperty("sourceSets")) {
            manifestFiles.addAll(FastdexUtils.getManifestFile(project,"main"))

            String buildTypeName = fastdexVariant.androidVariant.getBuildType().buildType.getName()
            String flavorName = fastdexVariant.androidVariant.flavorName

            if (buildTypeName && flavorName) {
                File file = FastdexUtils.getManifestFile(project,flavorName + buildTypeName.capitalize() as String)

                if (FileUtils.isLegalFile(file)) {
                    manifestFiles.add(file)
                }
            }

            if (buildTypeName) {
                File file = FastdexUtils.getManifestFile(project,buildTypeName)

                if (FileUtils.isLegalFile(file)) {
                    manifestFiles.add(file)
                }
            }

            if (flavorName) {
                File file = FastdexUtils.getManifestFile(project,flavorName)

                if (FileUtils.isLegalFile(file)) {
                    manifestFiles.add(file)
                }
            }
        }

        if (fastdexVariant.configuration.debug) {
            project.logger.error("==fastdex: manifestFiles ${manifestFiles}")
        }

        return manifestFiles
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

            File androidManifestStatFile = FastdexUtils.getAndroidManifestStatFile(fastdexVariant.project,fastdexVariant.variantName)
            andManifestDirectorySnapshoot.serializeTo(new FileOutputStream(androidManifestStatFile))
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
