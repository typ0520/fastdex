package com.dx168.fastdex.build.variant

import com.dx168.fastdex.build.extension.FastdexExtension
import com.dx168.fastdex.build.task.FastdexInstantRunTask
import fastdex.common.utils.SerializeUtils
import com.dx168.fastdex.build.util.LibDependency
import com.dx168.fastdex.build.util.MetaInfo
import com.dx168.fastdex.build.util.ProjectSnapshoot
import com.dx168.fastdex.build.util.FastdexUtils
import fastdex.common.utils.FileUtils
import com.dx168.fastdex.build.util.GradleUtils
import com.dx168.fastdex.build.util.TagManager
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Created by tong on 17/3/10.
 */
public class FastdexVariant {
    final Project project
    final FastdexExtension configuration
    final def androidVariant
    final String variantName
    final String manifestPath
    final File rootBuildDir
    final File buildDir
    final ProjectSnapshoot projectSnapshoot
    final TagManager tagManager
    final Set<LibDependency> libraryDependencies
    String originPackageName
    String mergedPackageName
    boolean hasDexCache
    boolean firstPatchBuild
    boolean initialized
    boolean hasJarMergingTask
    boolean executedJarMerge
    boolean executedDexTransform
    MetaInfo metaInfo
    FastdexInstantRunTask fastdexInstantRunTask

    FastdexVariant(Project project, Object androidVariant) {
        this.project = project
        this.androidVariant = androidVariant

        this.configuration = project.fastdex
        this.variantName = androidVariant.name.capitalize()
        this.manifestPath = androidVariant.outputs.first().processManifest.manifestOutputFile
        this.rootBuildDir = FastdexUtils.getBuildDir(project)
        this.buildDir = FastdexUtils.getBuildDir(project,variantName)

        projectSnapshoot = new ProjectSnapshoot(this)
        tagManager = new TagManager(this.project,this.variantName)
        libraryDependencies = LibDependency.resolveProjectDependency(project,androidVariant)

        if (configuration.dexMergeThreshold <= 0) {
            throw new GradleException("dexMergeThreshold Must be greater than 0!!")
        }
    }

    /*
    * 检查缓存是否过期，如果过期就删除
    * 1、查看app/build/fastdex/${variantName}/dex_cache目录下是否存在dex
    * 2、检查当前的依赖列表和全两打包时的依赖是否一致(app/build/fastdex/${variantName}/dependencies-mapping.txt)
    * 3、检查当前的依赖列表和全量打包时的依赖列表是否一致
    * 4、检查资源映射文件是否存在(app/build/fastdex/${variantName}/R.txt)
    * 5、检查全量的代码jar包是否存在(app/build/fastdex/${variantName}/injected-combined.jar)
    */
    void prepareEnv() {
        if (initialized) {
            return
        }
        initialized = true
        hasDexCache = FastdexUtils.hasDexCache(project,variantName)
        if (hasDexCache) {
            File diffResultSetFile = FastdexUtils.getDiffResultSetFile(project,variantName)
            if (!FileUtils.isLegalFile(diffResultSetFile)) {
                firstPatchBuild = true
            }

            try {
                metaInfo = MetaInfo.load(project,variantName)
                if (metaInfo == null) {
                    File metaInfoFile = FastdexUtils.getMetaInfoFile(project,variantName)

                    if (FileUtils.isLegalFile(metaInfoFile)) {
                        throw new CheckException("parse json content fail: ${FastdexUtils.getMetaInfoFile(project,variantName)}")
                    }
                    else {
                        throw new CheckException("miss meta info file: ${FastdexUtils.getMetaInfoFile(project,variantName)}")
                    }
                }

                File cachedDependListFile = FastdexUtils.getCachedDependListFile(project,variantName)
                if (!FileUtils.isLegalFile(cachedDependListFile)) {
                    throw new CheckException("miss depend list file: ${cachedDependListFile}")
                }

                File sourceSetSnapshootFile = FastdexUtils.getSourceSetSnapshootFile(project,variantName)
                if (!FileUtils.isLegalFile(sourceSetSnapshootFile)) {
                    throw new CheckException("miss sourceSet snapshoot file: ${sourceSetSnapshootFile}")
                }

                File resourceMappingFile = FastdexUtils.getResourceMappingFile(project,variantName)
                if (!FileUtils.isLegalFile(resourceMappingFile)) {
                    throw new CheckException("miss resource mapping file: ${resourceMappingFile}")
                }

                if (configuration.useCustomCompile) {
                    File injectedJarFile = FastdexUtils.getInjectedJarFile(project,variantName)
                    if (!FileUtils.isLegalFile(injectedJarFile)) {
                        throw new CheckException("miss injected jar file: ${injectedJarFile}")
                    }
                }

                try {
                    projectSnapshoot.loadSnapshoot()
                } catch (Throwable e) {
                    e.printStackTrace()
                    throw new CheckException(e)
                }

                if (projectSnapshoot.isDependenciesChanged()) {
                    throw new CheckException("dependencies changed")
                }
            } catch (CheckException e) {
                hasDexCache = false
                project.logger.error("==fastdex ${e.getMessage()}")
                project.logger.error("==fastdex we will remove ${variantName.toLowerCase()} cache")
            }
        }

        if (hasDexCache) {
            project.logger.error("==fastdex discover dex cache for ${variantName.toLowerCase()}")
        }
        else {
            metaInfo = new MetaInfo()
            metaInfo.projectPath = project.projectDir.absolutePath
            metaInfo.rootProjectPath = project.rootProject.projectDir.absolutePath
            metaInfo.variantName = variantName
            FastdexUtils.cleanCache(project,variantName)
            FileUtils.ensumeDir(buildDir)
        }

        projectSnapshoot.prepareEnv()
    }

    /**
     * 获取原始manifest文件的package节点的值
     * @return
     */
    public String getOriginPackageName() {
        if (originPackageName != null) {
            return originPackageName
        }
        String path = project.android.sourceSets.main.manifest.srcFile.absolutePath
        originPackageName = GradleUtils.getPackageName(path)
        return originPackageName
    }

    /**
     * 获取合并以后的manifest文件的package节点的值
     * @return
     */
    public String getMergedPackageName() {
        if (mergedPackageName != null) {
            return mergedPackageName
        }
        mergedPackageName = GradleUtils.getPackageName(manifestPath)
        return mergedPackageName
    }

    /**
     * 当dex生成以后
     * @param nornalBuild
     */
    public void onDexGenerateSuccess(boolean nornalBuild,boolean dexMerge) {
        if (nornalBuild) {
            saveMetaInfo()
            copyRTxt()
        }
        else {
            if (dexMerge) {
                //移除idx.xml public.xml
                File idsXmlFile = FastdexUtils.getIdxXmlFile(project,variantName)
                File publicXmlFile = FastdexUtils.getPublicXmlFile(project,variantName)
                FileUtils.deleteFile(idsXmlFile)
                FileUtils.deleteFile(publicXmlFile)

                copyRTxt()
            }
        }
        copyMetaInfo2Assets()
        projectSnapshoot.onDexGenerateSuccess(nornalBuild,dexMerge)
    }

    def saveMetaInfo() {
        File metaInfoFile = FastdexUtils.getMetaInfoFile(project,variantName)
        SerializeUtils.serializeTo(new FileOutputStream(metaInfoFile),metaInfo)
    }

    def copyMetaInfo2Assets() {
        File metaInfoFile = FastdexUtils.getMetaInfoFile(project,variantName)
        File assetsPath = androidVariant.getVariantData().getScope().getMergeAssetsOutputDir()
        FileUtils.copyFileUsingStream(metaInfoFile,new File(assetsPath,metaInfoFile.getName()))
    }

    /**
     * 保存资源映射文件
     */
    def copyRTxt() {
        File rtxtFile = new File(androidVariant.getVariantData().getScope().getSymbolLocation(),"R.txt")
        if (!FileUtils.isLegalFile(rtxtFile)) {
            rtxtFile = new File(project.buildDir,"${File.separator}intermediates${File.separator}symbols${File.separator}${androidVariant.dirName}${File.separator}R.txt")
        }
        FileUtils.copyFileUsingStream(rtxtFile,FastdexUtils.getResourceMappingFile(project,variantName))
    }

    /**
     * 补丁打包是否需要执行dex merge
     * @return
     */
    public boolean willExecDexMerge() {
        return hasDexCache && projectSnapshoot.diffResultSet.changedJavaFileDiffInfos.size() >= configuration.dexMergeThreshold
    }

    private class CheckException extends Exception {
        CheckException(String var1) {
            super(var1)
        }

        CheckException(Throwable var1) {
            super(var1)
        }
    }
}
