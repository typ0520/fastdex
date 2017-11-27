package fastdex.build.transform

import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformInvocationBuilder
import com.android.build.gradle.internal.transforms.DexTransform
import fastdex.build.util.Constants
import fastdex.build.util.FastdexUtils
import fastdex.build.util.GradleUtils
import fastdex.build.variant.FastdexVariant
import com.google.common.collect.Lists
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import fastdex.build.util.JarOperation

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
 * @see fastdex.build.task.FastdexResourceIdTask
 *
 * 补丁打包时的流程
 * 1、检查缓存的有效性
 * @see fastdex.build.task.FastdexCustomJavacTask 的prepareEnv方法说明
 * 2、扫描所有变化的java文件并编译成class
 * @see fastdex.build.task.FastdexCustomJavacTask
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
class FastdexDexTransform extends TransformProxy {

    FastdexDexTransform(Transform base,File streamOutputFolder, FastdexVariant fastdexVariant) {
        super(replaceBaseTransform(base, fastdexVariant),streamOutputFolder,fastdexVariant)
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, IOException, InterruptedException {
        File mainDexListFile = fastdexVariant.androidVariant.getVariantData().getScope().getMainDexListFile()

        if (!mainDexListFile.parentFile.exists()) {
            mainDexListFile.parentFile.mkdirs()
        }
        if (!mainDexListFile.exists()) {
            mainDexListFile.createNewFile()
        }

        if (fastdexVariant.hasDexCache) {
            project.logger.error("\n==fastdex patch transform start,we will generate dex file")
            if (fastdexVariant.projectSnapshoot.diffResultSet.isJavaFileChanged()) {
                //生成补丁jar包
                File patchJar = generatePatchJar(transformInvocation)
                //获取dex输出路径
                File dexOutputDir = GradleUtils.getDexOutputDir(transformInvocation)
                fastdexBuilder.patchBuild(base,patchJar,dexOutputDir)
            }
            else {
                project.logger.error("==fastdex no java files have changed, just ignore")
            }
        }
        else {
            project.logger.error("==fastdex normal transform start")

            if (!fastdexVariant.hasJarMergingTask) {
                fastdexBuilder.injectInputAndSaveClassPath(transformInvocation)
            }

            if (fastdexVariant.hasJarMergingTask && GradleUtils.getAndroidGradlePluginVersion().compareTo(Constants.MIN_BUILD_CACHE_ENABLED_VERSION) >= 0) {
                TransformInvocationBuilder builder = new TransformInvocationBuilder(transformInvocation.context)
                builder.addInputs(fastdexVariant.transformInvocation.inputs)
                builder.addReferencedInputs(fastdexVariant.transformInvocation.referencedInputs)
                builder.addSecondaryInputs(fastdexVariant.transformInvocation.secondaryInputs)
                builder.setIncrementalMode(transformInvocation.incremental)
                builder.addOutputProvider(transformInvocation.outputProvider)

                transformInvocation = builder.build()
            }

            fastdexBuilder.invokeNormalBuildTransform(base,transformInvocation)
            project.logger.error("==fastdex normal transform end\n")
        }
    }



    /**
     * 获取输出jar路径
     * @param invocation
     * @return
     */
    def getCombinedJarFile(TransformInvocation invocation) {
        List<JarInput> jarInputs = Lists.newArrayList()
        for (TransformInput input : invocation.getInputs()) {
            jarInputs.addAll(input.getJarInputs())
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
    def generatePatchJar(TransformInvocation transformInvocation) {
        if (fastdexVariant.hasJarMergingTask) {
            //如果开启了multidex,FastdexJarMergingTransform完成了jar merge的操作
            File patchJar = getCombinedJarFile(transformInvocation)
            project.logger.error("==fastdex multiDex enabled use patch.jar: ${patchJar}")
            return patchJar
        }
        else if (fastdexVariant.hasPreDexTask) {
            File patchJar = new File(fastdexVariant.preDexOutputFolder,Constants.PATCH_JAR)
            return patchJar
        }
        else {
            //补丁jar
            File patchJar = new File(FastdexUtils.getBuildDir(project,variantName),Constants.PATCH_JAR)
            //生成补丁jar
            JarOperation.generatePatchJar(fastdexVariant,transformInvocation,patchJar)
            return patchJar
        }
    }

    static Transform replaceBaseTransform(Transform base, FastdexVariant fastdexVariant) {
        if (GradleUtils.getAndroidGradlePluginVersion().compareTo(Constants.MIN_BUILD_CACHE_ENABLED_VERSION) >= 0 && GradleUtils.getAndroidGradlePluginVersion().compareTo("3.0") < 0) {
            //为了触发dex merge，使mainDexListFile不等于null

            //boolean needMerge = !multiDex || mainDexListFile != null;

            File mainDexListFile = base.mainDexListFile
            if (mainDexListFile == null) {
                mainDexListFile = fastdexVariant.androidVariant.getVariantData().getScope().getMainDexListFile()
            }

            fastdexVariant.project.logger.error("==fastdex android gradle >= ${Constants.MIN_BUILD_CACHE_ENABLED_VERSION} ,replace dex transform")
            return new DexTransform(
                    base.dexOptions,
                    base.debugMode,
                    base.multiDex,
                    mainDexListFile,
                    base.intermediateFolder,
                    base.androidBuilder,
                    base.logger.logger,
                    base.instantRunBuildContext,
                    base.buildCache)
        }
        else {
            return base
        }
    }
}