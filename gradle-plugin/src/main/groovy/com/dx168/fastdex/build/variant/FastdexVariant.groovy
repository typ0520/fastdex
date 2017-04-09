package com.dx168.fastdex.build.variant

import com.dx168.fastdex.build.extension.FastdexExtension
import com.dx168.fastdex.build.util.ProjectSnapshoot
import com.dx168.fastdex.build.util.FastdexUtils
import com.dx168.fastdex.build.util.FileUtils
import com.dx168.fastdex.build.util.GradleUtils
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
    boolean hasDexCache
    boolean firstPatchBuild

    FastdexVariant(Project project, Object androidVariant) {
        this.project = project
        this.androidVariant = androidVariant

        this.configuration = project.fastdex
        this.variantName = androidVariant.name.capitalize()
        this.manifestPath = androidVariant.outputs.first().processManifest.manifestOutputFile
        this.rootBuildDir = FastdexUtils.getBuildDir(project)
        this.buildDir = FastdexUtils.getBuildDir(project,variantName)

        projectSnapshoot = new ProjectSnapshoot(this)
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
        hasDexCache = FastdexUtils.hasDexCache(project,variantName)
        if (hasDexCache) {
            File diffResultSetFile = FastdexUtils.getDiffResultSetFile(project,variantName)
            if (!FileUtils.isLegalFile(diffResultSetFile)) {
                firstPatchBuild = true
            }
        }

        if (hasDexCache) {
            try {
                File cachedDependListFile = FastdexUtils.getCachedDependListFile(project,variantName)
                if (!FileUtils.isLegalFile(cachedDependListFile)) {
                    throw new CheckException("miss depend list file: ${cachedDependListFile}")
                }
                //old
                Set<String> cachedDependencies = FastdexUtils.getCachedDependList(project,variantName)
                //current
                Set<String> currentDependencies = GradleUtils.getCurrentDependList(project,androidVariant)
                currentDependencies.removeAll(cachedDependencies)

                //check dependencies
                //remove
                //old    current
                //1.aar  1.aar
                //2.aar

                //add
                //old    current
                //1.aar  1.aar
                //       2.aar

                //change
                //old    current
                //1.aar  1.aar
                //2.aar  xx.aar

                //handler add and change
                if (!currentDependencies.isEmpty()) {
                    throw new CheckException("${variantName.toLowerCase()} dependencies changed")
                }

                File sourceSetSnapshootFile = FastdexUtils.getSourceSetSnapshootFile(project,variantName)
                if (!FileUtils.isLegalFile(sourceSetSnapshootFile)) {
                    throw new CheckException("miss sourceSet snapshoot file: ${sourceSetSnapshootFile}")
                }

                File cachedResourceMappingFile = FastdexUtils.getCachedResourceMappingFile(project,variantName)
                if (!FileUtils.isLegalFile(cachedResourceMappingFile)) {
                    throw new CheckException("miss resource mapping file: ${cachedResourceMappingFile}")
                }

                File injectedJarFile = FastdexUtils.getInjectedJarFile(project,variantName)
                if (!FileUtils.isLegalFile(injectedJarFile)) {
                    throw new CheckException("miss injected jar file: ${injectedJarFile}")
                }
            } catch (CheckException e) {
                hasDexCache = false
                project.logger.error("==fastdex ${e.getMessage()}")
                project.logger.error("==fastdex we will remove ${variantName.toLowerCase()} cache")
            }
        }

        if (hasDexCache) {
            project.logger.error("==fastdex discover cached for ${variantName.toLowerCase()}")
        }
        else {
            FastdexUtils.cleanCache(project,variantName)
            FileUtils.ensumeDir(buildDir)
        }

        projectSnapshoot.prepareEnv()
    }

    public String getApplicationPackageName() {
        return GradleUtils.getPackageName(manifestPath)
    }

    private class CheckException extends Exception {
        CheckException(String var1) {
            super(var1)
        }
    }
}
