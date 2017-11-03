package fastdex.build.util

import com.android.build.gradle.api.BaseVariant
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
class ProjectSnapshoot {
    private static final boolean DEBUG_SNAPSHOOT = false

    final FastdexVariant fastdexVariant
    SourceSetSnapshoot sourceSetSnapshoot
    SourceSetSnapshoot oldSourceSetSnapshoot
    SourceSetDiffResultSet diffResultSet
    SourceSetDiffResultSet oldDiffResultSet
    StringSnapshoot dependenciesSnapshoot
    StringSnapshoot oldDependenciesSnapshoot
    JavaDirectorySnapshoot oldAptJavaDirectorySnapshoot
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
    }

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

        for (LibDependency libDependency : fastdexVariant.getLibraryDependencies()) {
            if (libDependency.androidLibrary) {
                File file = libDependency.dependencyProject.android.sourceSets.main.manifest.srcFile
                andManifestDirectorySnapshoot.addFile(file)
            }
        }

        if (fastdexVariant.hasDexCache) {
            if (oldSourceSetSnapshoot != null) {
                //移除apt目录
                File aptDir = GradleUtils.getAptOutputDir(fastdexVariant.androidVariant)
                oldAptJavaDirectorySnapshoot = oldSourceSetSnapshoot.removeJavaDirectorySnapshootByPath(aptDir.absolutePath)

                project.logger.error("==fastdex remove apt-dir from old snapshoot.")
            }

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
        for (LibDependency libDependency : fastdexVariant.getLibraryDependencies()) {
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
        if (GradleUtils.getAndroidGradlePluginVersion().compareTo("3.0") >= 0) {
            //3.0之前默认依赖release，3.0依赖Application工程的buildType对应的library工程的buildType
            libraryVariantdirName = fastdexVariant.androidVariant.getBuildType().buildType.getName()
        }

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
            String packageName = (i == 0 ? fastdexVariant.getOriginPackageName() : GradleUtils.getPackageName(project.android.sourceSets.main.manifest.srcFile.absolutePath))
            String packageNamePath = packageName.split("\\.").join(File.separator)
            //buildconfig
            String buildConfigJavaRelativePath = "${packageNamePath}${File.separator}BuildConfig.java"
            String rJavaRelativePath = "${packageNamePath}${File.separator}R.java"

            BaseVariant baseVariant = (i == 0 ? fastdexVariant.androidVariant : GradleUtils.getLibraryFirstVariant(project,libraryVariantdirName))

            File buildConfigDir = baseVariant.getVariantData().getScope().getBuildConfigSourceOutputDir()
            File rDir = baseVariant.getVariantData().getScope().getRClassSourceOutputDir()
            File rsDir = baseVariant.getVariantData().getScope().getRenderscriptSourceOutputDir()
            File aidlDir = baseVariant.getVariantData().getScope().getAidlSourceOutputDir()
            File aptDir = GradleUtils.getAptOutputDir(fastdexVariant.androidVariant)

            //buildconfig
            File buildConfigJavaFile = new File(buildConfigDir,buildConfigJavaRelativePath)
            JavaDirectorySnapshoot buildConfigSnapshoot = new JavaDirectorySnapshoot(buildConfigDir,true,buildConfigJavaFile.absolutePath)
            buildConfigSnapshoot.projectPath = project.projectDir.absolutePath
            snapshoot.addJavaDirectorySnapshoot(buildConfigSnapshoot)

            //r
            File rJavaFile = new File(rDir,rJavaRelativePath)
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

            //忽略掉apt目录
            boolean skipAppProjectAptDir = (i == 0)
            if (!skipAppProjectAptDir) {
                //apt
                JavaDirectorySnapshoot aptDirectorySnapshoot = new JavaDirectorySnapshoot(aptDir,true)
                aptDirectorySnapshoot.projectPath = project.projectDir.absolutePath
                snapshoot.addJavaDirectorySnapshoot(aptDirectorySnapshoot)
            }
            else {
                project.logger.error("==fastdex skip scan: ${aptDir}")
            }
        }
    }

    /**
     * 往源码快照里添加依赖的工程源码路径
     * @param snapshoot
     */
    def handleLibraryDependencies(SourceSetSnapshoot snapshoot) {
        if (DEBUG_SNAPSHOOT) {
            fastdexVariant.project.logger.error("==fastdex: libraryDependencies: ${fastdexVariant.getLibraryDependencies()}")
        }
        for (LibDependency libDependency : fastdexVariant.getLibraryDependencies()) {
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
            //  https://developer.android.com/studio/build/build-variants.html

            /**
             src/main/
             此源集包括所有构建变体共用的代码和资源。

             src/<buildType>/
             创建此源集可加入特定构建类型专用的代码和资源。

             src/<productFlavor>/
             创建此源集可加入特定产品风味专用的代码和资源。

             src/<productFlavorBuildType>/
             创建此源集可加入特定构建变体专用的代码和资源。
             */

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

        if (DEBUG_SNAPSHOOT) {
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
