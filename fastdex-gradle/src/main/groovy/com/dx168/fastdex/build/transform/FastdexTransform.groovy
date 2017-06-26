package com.dx168.fastdex.build.transform

import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.dx168.fastdex.build.util.ClassInject
import com.dx168.fastdex.build.util.Constants
import com.dx168.fastdex.build.util.DexOperation
import com.dx168.fastdex.build.util.FastdexUtils
import com.dx168.fastdex.build.util.GradleUtils
import com.dx168.fastdex.build.variant.FastdexVariant
import com.google.common.collect.Lists
import org.gradle.api.Project
import fastdex.common.utils.FileUtils
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.dx168.fastdex.build.util.JarOperation

/**
 * 用于dex生成
 * 全量打包时的流程:
 * 1、合并所有的class文件生成一个jar包
 * 2、扫描所有的项目代码并且在构造方法里添加对com.dx168.fastdex.runtime.antilazyload.AntilazyLoad类的依赖
 *    这样做的目的是为了解决class verify的问题，
 *    详情请看https://mp.weixin.qq.com/s?__biz=MzI1MTA1MzM2Nw==&mid=400118620&idx=1&sn=b4fdd5055731290eef12ad0d17f39d4a
 * 3、对项目代码做快照，为了以后补丁打包时对比那些java文件发生了变化
 * 4、对当前项目的所以依赖做快照，为了以后补丁打包时对比依赖是否发生了变化，如果变化需要清除缓存
 * 5、调用真正的transform生成dex
 * 6、缓存生成的dex，并且把fastdex-runtime.dex插入到dex列表中，假如生成了两个dex，classes.dex classes2.dex 需要做一下操作
 *    fastdex-runtime.dex => classes.dex
 *    classes.dex         => classes2.dex
 *    classes2.dex        => classes3.dex
 *    然后运行期在入口Application(com.dx168.fastdex.runtime.FastdexApplication)使用MultiDex把所有的dex加载进来
 * 7、保存资源映射映射表，为了保持id的值一致，详情看
 * @see com.dx168.fastdex.build.task.FastdexResourceIdTask
 *
 * 补丁打包时的流程
 * 1、检查缓存的有效性
 * @see com.dx168.fastdex.build.task.FastdexCustomJavacTask 的prepareEnv方法说明
 * 2、扫描所有变化的java文件并编译成class
 * @see com.dx168.fastdex.build.task.FastdexCustomJavacTask
 * 3、合并所有变化的class并生成jar包
 * 4、生成补丁dex
 * 5、把所有的dex按照一定规律放在transformClassesWithMultidexlistFor${variantName}任务的输出目录
 *    fastdex-runtime.dex    => classes.dex
 *    patch.dex              => classes2.dex
 *    dex_cache.classes.dex  => classes3.dex
 *    dex_cache.classes2.dex => classes4.dex
 *    dex_cache.classesN.dex => classes(N + 2).dex
 *
 * Created by tong on 17/10/3.
 */
class FastdexTransform extends TransformProxy {
    FastdexVariant fastdexVariant

    Project project
    String variantName

    FastdexTransform(Transform base, FastdexVariant fastdexVariant) {
        super(base)
        this.fastdexVariant = fastdexVariant
        this.project = fastdexVariant.project
        this.variantName = fastdexVariant.variantName
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, IOException, InterruptedException {
        if (fastdexVariant.hasDexCache) {
            project.logger.error("==fastdex patch transform start,we will generate dex file")
            if (fastdexVariant.projectSnapshoot.diffResultSet.isJavaFileChanged()) {
                //生成补丁jar包
                File patchJar = generatePatchJar(transformInvocation)
                File patchDex = FastdexUtils.getPatchDexFile(fastdexVariant.project,fastdexVariant.variantName)

                DexOperation.generatePatchDex(fastdexVariant,base,patchJar,patchDex)
                //获取dex输出路径
                File dexOutputDir = GradleUtils.getDexOutputDir(project,base,transformInvocation)
                //merged dex
                File mergedPatchDexDir = FastdexUtils.getMergedPatchDexDir(fastdexVariant.project,fastdexVariant.variantName)

                if (fastdexVariant.willExecDexMerge()) {
                    //merge dex
                    if (FileUtils.hasDex(mergedPatchDexDir)) {
                        //已经执行过一次dex merge
                        File cacheDexDir = FastdexUtils.getDexCacheDir(project,variantName)
                        //File outputDex = new File(dexOutputDir,"merged-patch.dex")
                        File mergedPatchDex = new File(mergedPatchDexDir,Constants.CLASSES_DEX)
                        //更新patch.dex
                        DexOperation.mergeDex(fastdexVariant,mergedPatchDex,patchDex,mergedPatchDex)
                        FileUtils.cleanDir(dexOutputDir)

                        FileUtils.copyDir(cacheDexDir,dexOutputDir,Constants.DEX_SUFFIX)

                        incrementDexDir(dexOutputDir,2)
                        //copy merged-patch.dex
                        FileUtils.copyFileUsingStream(mergedPatchDex,new File(dexOutputDir,"${Constants.CLASSES}2${Constants.DEX_SUFFIX}"))
                        //copy fastdex-runtime.dex
                        FileUtils.copyResourceUsingStream(Constants.RUNTIME_DEX_FILENAME,new File(dexOutputDir,Constants.CLASSES_DEX))
                    }
                    else {
                        //第一只执行dex merge,直接保存patchDex
                        //patch.dex              => classes.dex
                        //dex_cache.classes.dex  => classes2.dex
                        //dex_cache.classes2.dex => classes3.dex
                        //dex_cache.classesN.dex => classes(N + 1).dex
                        //复制补丁dex到输出路径
                        hookPatchBuildDex(dexOutputDir,mergedPatchDexDir,patchDex)

                        FileUtils.cleanDir(mergedPatchDexDir)
                        FileUtils.ensumeDir(mergedPatchDexDir)
                        patchDex.renameTo(new File(mergedPatchDexDir,Constants.CLASSES_DEX))
                    }
                    fastdexVariant.onDexGenerateSuccess(false,true)
                }
                else {
                    fastdexVariant.metaInfo.patchDexVersion += 1
                    //复制补丁打包的dex到输出路径
                    hookPatchBuildDex(dexOutputDir,mergedPatchDexDir,patchDex)
                    fastdexVariant.onDexGenerateSuccess(false,false)
                }
            }
            else {
                project.logger.error("==fastdex no java files have changed, just ignore")
            }
        }
        else {
            def config = fastdexVariant.androidVariant.getVariantData().getVariantConfiguration()
            boolean isMultiDexEnabled = config.isMultiDexEnabled()

            project.logger.error("==fastdex normal transform start")
            if (isMultiDexEnabled) {
                if (fastdexVariant.executedJarMerge) {
                    //如果开启了multidex,FastdexJarMergingTransform完成了inject的操作，不需要在做处理
                    File combinedJar = getCombinedJarFile(transformInvocation)

                    if (fastdexVariant.configuration.useCustomCompile) {
                        File injectedJar = FastdexUtils.getInjectedJarFile(project,variantName)
                        FileUtils.copyFileUsingStream(combinedJar,injectedJar)
                    }
                } else {
                    ClassInject.injectTransformInvocation(fastdexVariant,transformInvocation)
                    File injectedJar = FastdexUtils.getInjectedJarFile(project,variantName)
                    GradleUtils.executeMerge(project,transformInvocation,injectedJar)
                    transformInvocation = GradleUtils.createNewTransformInvocation(base,transformInvocation,injectedJar)
                }
            }
            else {
                //如果没有开启multidex需要在此处做注入
                ClassInject.injectTransformInvocation(fastdexVariant,transformInvocation)
                if (fastdexVariant.configuration.useCustomCompile) {
                    File injectedJar = FastdexUtils.getInjectedJarFile(project,variantName)
                    GradleUtils.executeMerge(project,transformInvocation,injectedJar)
                }
            }
            //调用默认转换方法
            base.transform(transformInvocation)
            //获取dex输出路径
            File dexOutputDir = GradleUtils.getDexOutputDir(project,base,transformInvocation)
            //缓存dex
            int dexCount = cacheNormalBuildDex(dexOutputDir)
            //复制全量打包的dex到输出路径
            hookNormalBuildDex(dexOutputDir)

            fastdexVariant.metaInfo.dexCount = dexCount
            fastdexVariant.metaInfo.buildMillis = System.currentTimeMillis()

            fastdexVariant.onDexGenerateSuccess(true,false)
            project.logger.error("==fastdex normal transform end")
        }

        fastdexVariant.executedDexTransform = true
    }

    /**
     * 获取输出jar路径
     * @param invocation
     * @return
     */
    public File getCombinedJarFile(TransformInvocation invocation) {
        List<JarInput> jarInputs = Lists.newArrayList();
        for (TransformInput input : invocation.getInputs()) {
            jarInputs.addAll(input.getJarInputs());
        }
        if (jarInputs.size() != 1) {
            throw new RuntimeException("==fastdex jar input size is ${jarInputs.size()}, expected is 1")
        }
        File combinedJar = jarInputs.get(0).getFile()
        return combinedJar
    }

    /**
     * 生成补丁jar包
     * @param transformInvocation
     * @return
     */
    File generatePatchJar(TransformInvocation transformInvocation) {
        def config = fastdexVariant.androidVariant.getVariantData().getVariantConfiguration()
        boolean isMultiDexEnabled = config.isMultiDexEnabled()
        if (isMultiDexEnabled && (fastdexVariant.executedJarMerge || fastdexVariant.hasJarMergingTask)) {
            //如果开启了multidex,FastdexJarMergingTransform完成了jar merge的操作
            File patchJar = getCombinedJarFile(transformInvocation)
            project.logger.error("==fastdex multiDex enabled use patch.jar: ${patchJar}")
            return patchJar
        }
        else {
            //补丁jar
            File patchJar = new File(FastdexUtils.getBuildDir(project,variantName),"patch-combined.jar")
            //生成补丁jar
            JarOperation.generatePatchJar(fastdexVariant,transformInvocation,patchJar)
            return patchJar
        }
    }

    /**
     * 缓存全量打包时生成的dex
     * @param dexOutputDir dex输出路径
     */
    int cacheNormalBuildDex(File dexOutputDir) {
        project.logger.error("==fastdex dex output directory: " + dexOutputDir)

        int dexCount = 0
        File cacheDexDir = FastdexUtils.getDexCacheDir(project,variantName)
        File[] files = dexOutputDir.listFiles()
        files.each { file ->
            if (file.getName().endsWith(Constants.DEX_SUFFIX)) {
                FileUtils.copyFileUsingStream(file,new File(cacheDexDir,file.getName()))
                dexCount = dexCount + 1
            }
        }
        return dexCount
    }

    void incrementDexDir(File dexDir) {
        incrementDexDir(dexDir,1)
    }

    /**
     * 递增指定目录中的dex
     *
     * classes.dex   => classes2.dex
     * classes2.dex  => classes3.dex
     * classesN.dex  => classes(N + 1).dex
     *
     * @param dexDir
     */
    void incrementDexDir(File dexDir,int dsize) {
        if (dsize <= 0) {
            throw new RuntimeException("dsize must be greater than 0!")
        }
        //classes.dex  => classes2.dex.tmp
        //classes2.dex => classes3.dex.tmp
        //classesN.dex => classes(N + 1).dex.tmp

        String tmpSuffix = ".tmp"
        File classesDex = new File(dexDir,Constants.CLASSES_DEX)
        if (FileUtils.isLegalFile(classesDex)) {
            classesDex.renameTo(new File(dexDir,"classes${dsize + 1}.dex${tmpSuffix}"))
        }
        int point = 2
        File dexFile = new File(dexDir,"${Constants.CLASSES}${point}${Constants.DEX_SUFFIX}")
        while (FileUtils.isLegalFile(dexFile)) {
            new File(dexDir,"classes${point}.dex").renameTo(new File(dexDir,"classes${point + dsize}.dex${tmpSuffix}"))
            point++
            dexFile = new File(dexDir,"classes${point}.dex")
        }

        //classes2.dex.tmp => classes2.dex
        //classes3.dex.tmp => classes3.dex
        //classesN.dex.tmp => classesN.dex
        point = dsize + 1
        dexFile = new File(dexDir,"classes${point}.dex${tmpSuffix}")
        while (FileUtils.isLegalFile(dexFile)) {
            dexFile.renameTo(new File(dexDir,"classes${point}.dex"))
            point++
            dexFile = new File(dexDir,"classes${point}.dex${tmpSuffix}")
        }
    }

    /**
     * 全量打包时复制dex到指定位置
     * @param dexOutputDir dex输出路径
     */
    void hookNormalBuildDex(File dexOutputDir) {
        //dexelements [fastdex-runtime.dex ${dex_cache}.listFiles]
        //runtime.dex            => classes.dex
        //dex_cache.classes.dex  => classes2.dex
        //dex_cache.classes2.dex => classes3.dex
        //dex_cache.classesN.dex => classes(N + 1).dex

        incrementDexDir(dexOutputDir)

        //fastdex-runtime.dex = > classes.dex
        FileUtils.copyResourceUsingStream(Constants.RUNTIME_DEX_FILENAME,new File(dexOutputDir,Constants.CLASSES_DEX))
        printLogWhenDexGenerateComplete(dexOutputDir,true)
    }

    /**
     * 补丁打包时复制dex到指定位置
     * @param dexOutputDir dex输出路径
     */
    void hookPatchBuildDex(File dexOutputDir,File mergedPatchDexDir,File patchDex) {
        //dexelements [fastdex-runtime.dex patch.dex ${dex_cache}.listFiles]
        //runtime.dex            => classes.dex
        //patch.dex              => classes2.dex
        //dex_cache.classes.dex  => classes3.dex
        //dex_cache.classes2.dex => classes4.dex
        //dex_cache.classesN.dex => classes(N + 2).dex
        project.logger.error("==fastdex patch transform hook patch dex start")

        FileUtils.cleanDir(dexOutputDir)
        File mergedPatchDex = new File(mergedPatchDexDir,Constants.CLASSES_DEX)
        File cacheDexDir = FastdexUtils.getDexCacheDir(project,variantName)

        //copy fastdex-runtime.dex
        FileUtils.copyResourceUsingStream(Constants.RUNTIME_DEX_FILENAME,new File(dexOutputDir,Constants.CLASSES_DEX))
        //copy patch.dex
        FileUtils.copyFileUsingStream(patchDex,new File(dexOutputDir,"classes2.dex"))
        if (FileUtils.fileExists(mergedPatchDex.absolutePath)) {
            FileUtils.copyFileUsingStream(mergedPatchDex,new File(dexOutputDir,"classes3.dex"))
            FileUtils.copyFileUsingStream(new File(cacheDexDir,Constants.CLASSES_DEX),new File(dexOutputDir,"classes4.dex"))

            int point = 2
            File dexFile = new File(cacheDexDir,"${Constants.CLASSES}${point}${Constants.DEX_SUFFIX}")
            while (FileUtils.isLegalFile(dexFile)) {
                FileUtils.copyFileUsingStream(dexFile,new File(dexOutputDir,"${Constants.CLASSES}${point + 3}${Constants.DEX_SUFFIX}"))
                point++
                dexFile = new File(cacheDexDir,"${Constants.CLASSES}${point}${Constants.DEX_SUFFIX}")
            }
        }
        else {
            FileUtils.copyFileUsingStream(new File(cacheDexDir,Constants.CLASSES_DEX),new File(dexOutputDir,"classes3.dex"))
            int point = 2
            File dexFile = new File(cacheDexDir,"${Constants.CLASSES}${point}${Constants.DEX_SUFFIX}")
            while (FileUtils.isLegalFile(dexFile)) {
                FileUtils.copyFileUsingStream(dexFile,new File(dexOutputDir,"${Constants.CLASSES}${point + 2}${Constants.DEX_SUFFIX}"))
                point++
                dexFile = new File(cacheDexDir,"${Constants.CLASSES}${point}${Constants.DEX_SUFFIX}")
            }
        }
        printLogWhenDexGenerateComplete(dexOutputDir,false)
    }

    /**
     * 当dex生成完成后打印日志
     * @param normalBuild
     */
    void printLogWhenDexGenerateComplete(File dexOutputDir,boolean normalBuild) {
        File cacheDexDir = FastdexUtils.getDexCacheDir(project,variantName)

        //log
        StringBuilder sb = new StringBuilder()
        sb.append("cached_dex[")
        File[] dexFiles = cacheDexDir.listFiles()
        for (File file : dexFiles) {
            if (file.getName().endsWith(Constants.DEX_SUFFIX)) {
                sb.append(file.getName())
                if (file != dexFiles[dexFiles.length - 1]) {
                    sb.append(",")
                }
            }
        }
        sb.append("] cur-dex[")
        dexFiles = dexOutputDir.listFiles()
        int idx = 0
        for (File file : dexFiles) {
            if (file.getName().endsWith(Constants.DEX_SUFFIX)) {
                sb.append(file.getName())
                if (idx < (dexFiles.length - 1)) {
                    sb.append(",")
                }
            }
            idx ++
        }
        sb.append("]")
        if (normalBuild) {
            project.logger.error("==fastdex first build ${sb}")
        }
        else {
            project.logger.error("==fastdex patch build ${sb}")
        }
    }
}