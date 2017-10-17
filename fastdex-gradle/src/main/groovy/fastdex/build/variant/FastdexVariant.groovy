package fastdex.build.variant

import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.api.ApplicationVariant
import com.github.typ0520.fastdex.Version
import fastdex.build.extension.FastdexExtension
import fastdex.build.task.FastdexInstantRunTask
import fastdex.build.transform.FastdexTransform
import fastdex.build.util.Constants
import fastdex.build.util.FastdexInstantRun
import fastdex.build.util.FastdexRuntimeException
import fastdex.build.util.JumpException
import fastdex.common.utils.SerializeUtils
import fastdex.build.util.LibDependency
import fastdex.build.util.MetaInfo
import fastdex.build.util.ProjectSnapshoot
import fastdex.build.util.FastdexUtils
import fastdex.common.utils.FileUtils
import fastdex.build.util.GradleUtils
import org.gradle.api.Project

/**
 * Created by tong on 17/3/10.
 */
public class FastdexVariant {
    final Project project
    final FastdexExtension configuration
    final ApplicationVariant androidVariant
    final String variantName
    final String manifestPath
    final File rootBuildDir
    final File buildDir
    final ProjectSnapshoot projectSnapshoot
    final Set<LibDependency> libraryDependencies

    String originPackageName
    String mergedPackageName
    boolean hasDexCache
    boolean firstPatchBuild
    boolean initialized
    boolean needExecDexMerge
    boolean hasJarMergingTask
    boolean compiledByCustomJavac
    boolean compiledByOriginJavac
    MetaInfo metaInfo
    FastdexTransform fastdexTransform
    FastdexInstantRun fastdexInstantRun
    FastdexInstantRunTask fastdexInstantRunTask

    TransformInvocation transformInvocation

    FastdexVariant(Project project, Object androidVariant) {
        this.project = project
        this.androidVariant = androidVariant

        this.configuration = project.fastdex
        this.variantName = androidVariant.name.capitalize()
        this.manifestPath = androidVariant.outputs.first().processManifest.manifestOutputFile
        this.rootBuildDir = FastdexUtils.getBuildDir(project)
        this.buildDir = FastdexUtils.getBuildDir(project,variantName)

        projectSnapshoot = new ProjectSnapshoot(this)
        libraryDependencies = LibDependency.resolveProjectDependency(project,androidVariant)

        if (configuration.dexMergeThreshold <= 1) {
            throw new FastdexRuntimeException("DexMergeThreshold must be greater than 1!!")
        }
    }

    /*
    * 检查缓存是否过期，如果过期就删除
    */
    void prepareEnv() {
        if (initialized) {
            return
        }
        initialized = true
        hasDexCache = FastdexUtils.hasDexCache(project,variantName)

        project.logger.error("==fastdex hasDexCache: ${hasDexCache}")
        if (hasDexCache) {
            File diffResultSetFile = FastdexUtils.getDiffResultSetFile(project,variantName)
            if (!FileUtils.isLegalFile(diffResultSetFile)) {
                firstPatchBuild = true
            }

            try {
                //检查meta-info文件是否存在(app/build/fastdex/${variantName}/fastdex-meta-info.json)
                File metaInfoFile = FastdexUtils.getMetaInfoFile(project,variantName)
                if (!FileUtils.isLegalFile(metaInfoFile)) {
                    throw new JumpException("miss file : ${metaInfoFile}")
                }
                metaInfo = MetaInfo.load(project,variantName)
                if (metaInfo == null) {
                    throw new JumpException("parse json content fail: ${FastdexUtils.getMetaInfoFile(project,variantName)}")
                }

                if (metaInfo.fastdexVersion == null) {
                    throw new JumpException("cache already expired")
                }

                //检查当前的fastdexVersion和缓存对应的fastdexVersion是否一致
                if (metaInfo.fastdexVersion == null || !Version.FASTDEX_BUILD_VERSION.equals(metaInfo.fastdexVersion)) {
                    File dxJarFile = new File(FastdexUtils.getBuildDir(project),"fastdex-dx.jar")
                    File dxCommandFile = new File(FastdexUtils.getBuildDir(project),"fastdex-dx")
                    File fastdexRuntimeDex = new File(buildDir, Constants.RUNTIME_DEX_FILENAME)

                    FileUtils.deleteFile(dxJarFile)
                    FileUtils.deleteFile(dxCommandFile)
                    FileUtils.deleteFile(fastdexRuntimeDex)

                    throw new JumpException("cache fastdexVersion: ${metaInfo.fastdexVersion}, current fastdexVersion: ${Version.FASTDEX_BUILD_VERSION}")
                }

                //检查当前的依赖列表和全两打包时的依赖是否一致(app/build/fastdex/${variantName}/dependencies.json)
                File cachedDependListFile = FastdexUtils.getCachedDependListFile(project,variantName)
                if (!FileUtils.isLegalFile(cachedDependListFile)) {
                    throw new JumpException("miss depend list file: ${cachedDependListFile}")
                }

                //检查源码快照
                File sourceSetSnapshootFile = FastdexUtils.getSourceSetSnapshootFile(project,variantName)
                if (!FileUtils.isLegalFile(sourceSetSnapshootFile)) {
                    throw new JumpException("miss sourceSet snapshoot file: ${sourceSetSnapshootFile}")
                }

                //检查资源映射文件是否存在(app/build/fastdex/${variantName}/r/r.txt)
                File resourceMappingFile = FastdexUtils.getResourceMappingFile(project,variantName)
                if (!FileUtils.isLegalFile(resourceMappingFile)) {
                    throw new JumpException("miss resource mapping file: ${resourceMappingFile}")
                }

                //检查manifest快照
                File androidManifestStatFile = FastdexUtils.getAndroidManifestStatFile(project,variantName)
                if (!FileUtils.isLegalFile(androidManifestStatFile)) {
                    throw new JumpException("miss android manifest stat file: ${androidManifestStatFile}")
                }

                //如果执行过dex merge，检查merged-patch.dex是否存在
                if (metaInfo.mergedDexVersion > 0) {
                    File mergedPatchDex = FastdexUtils.getMergedPatchDex(project,variantName)
                    if (!FileUtils.isLegalFile(androidManifestStatFile)) {
                        throw new JumpException("miss merged dex file: ${mergedPatchDex}")
                    }
                }

                //如果开启了自定义的java编译任务，检查classpath文件
                if (configuration.useCustomCompile) {
                    File classpathFile = new File(FastdexUtils.getBuildDir(project,variantName),Constants.CLASSPATH_FILENAME)
                    if (!FileUtils.isLegalFile(classpathFile)) {
                        throw new JumpException("miss classpath file: ${classpathFile}")
                    }
                }

                //判断当前的工程目录和生成缓存时的工程目录是否一致
                String oldRootProjectPath = metaInfo.rootProjectPath
                String curRootProjectPath = project.rootProject.projectDir.absolutePath
                boolean isRootProjectDirChanged = metaInfo.isRootProjectDirChanged(curRootProjectPath)
                if (isRootProjectDirChanged) {
                    throw new JumpException("project path changed old: ${oldRootProjectPath} now: ${curRootProjectPath}")
                }
                projectSnapshoot.loadSnapshoot()
                //检查依赖是否发生变化
                if (projectSnapshoot.isDependenciesChanged()) {
                    throw new JumpException("dependencies changed")
                }
            } catch (Throwable e) {
                hasDexCache = false

                if (!(e instanceof JumpException) && configuration.debug) {
                    e.printStackTrace()
                }

                project.logger.error("==fastdex ${e.getMessage()}")
                project.logger.error("==fastdex we will remove ${variantName.toLowerCase()} cache")
            }
        }

        if (hasDexCache && metaInfo != null) {
            project.logger.error("==fastdex discover dex cache for ${variantName.toLowerCase()}")
        }
        else {
            metaInfo = new MetaInfo()
            metaInfo.fastdexVersion = Version.FASTDEX_BUILD_VERSION

            metaInfo.projectPath = project.projectDir.absolutePath
            metaInfo.rootProjectPath = project.rootProject.projectDir.absolutePath
            metaInfo.variantName = variantName
            FastdexUtils.cleanCache(project,variantName)
            FileUtils.ensumeDir(buildDir)
        }

        projectSnapshoot.prepareEnv()

        if (hasDexCache) {
            //判断下当前有多少个源文件发生变化，如果超过了阈值将会执行dex merge操作
            needExecDexMerge = projectSnapshoot.diffResultSet.addOrModifiedPathInfos.size() >= configuration.dexMergeThreshold
        }
        fastdexInstantRun.onFastdexPrepare()
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
        projectSnapshoot.onDexGenerateSuccess(nornalBuild,dexMerge)
        fastdexInstantRun.onSourceChanged()
    }

    /**
     * 保存fastdex-meta-info文件
     * @return
     */
    def saveMetaInfo() {
        File metaInfoFile = FastdexUtils.getMetaInfoFile(project,variantName)
        SerializeUtils.serializeTo(new FileOutputStream(metaInfoFile),metaInfo)
    }

    /**
     * 把fastdex-meta-info文件复制到合并以后的assets目录下
     * @return
     */
    def copyMetaInfo2Assets() {
        File metaInfoFile = FastdexUtils.getMetaInfoFile(project,variantName)
        if (FileUtils.isLegalFile(metaInfoFile)) {
            File assetsPath = androidVariant.getVariantData().getScope().getMergeAssetsOutputDir()
            File dest = new File(assetsPath,metaInfoFile.getName())

            project.logger.error("==fastdex copy meta info: \nfrom: " + metaInfoFile + "\ninto: " + dest)
            FileUtils.copyFileUsingStream(metaInfoFile,dest)
        }
    }

    /**
     * 执行package任务之前
     * @return
     */
    def onPrePackage() {
        copyMetaInfo2Assets()
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
    def willExecDexMerge() {
        return needExecDexMerge
    }
}
