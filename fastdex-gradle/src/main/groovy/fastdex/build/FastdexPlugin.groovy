package fastdex.build

import com.android.build.api.transform.Transform
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.transforms.DexTransform
import com.android.build.gradle.internal.transforms.JarMergingTransform
import fastdex.build.extension.FastdexExtension
import fastdex.build.task.*
import fastdex.build.transform.FastdexDexBuilderTransform
import fastdex.build.transform.FastdexDexMergerTransform
import fastdex.build.transform.FastdexDexTransform
import fastdex.build.transform.FastdexJarMergingTransform
import fastdex.build.transform.FastdexPreDexTransform
import fastdex.build.util.*
import fastdex.build.variant.FastdexVariant
import fastdex.common.utils.FileUtils
import org.gradle.api.*
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener
import java.lang.reflect.Field

/**
 * 注册相应节点的任务
 * Created by tong on 17/10/3.
 */
class FastdexPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('Android Application plugin required')
        }

        project.extensions.create('fastdex', FastdexExtension)
        FastdexBuildListener.addByProject(project)

        def android = project.extensions.android

        //close preDexLibraries
        android.dexOptions.preDexLibraries = false
        //open jumboMode
        android.dexOptions.jumboMode = true

        if (GradleUtils.getAndroidGradlePluginVersion().compareTo("3.0.0") >= 0) {
            //禁止掉aapt2
            reflectAapt2Flag(project)
            //禁止掉dex archive任务
            reflectDexArchive(project)
        }

        project.afterEvaluate {
            def configuration = project.fastdex
            //如果是fastdex的插件触发的打包，开启hook
            if (project.hasProperty("fastdex.injected.invoked.from.ide")) {
                configuration.fastdexEnable = true
            }
            if (!configuration.fastdexEnable) {
                project.logger.error("====fastdex tasks are disabled.====")
                return
            }

            //https://developer.android.com/studio/build/build-cache.html
            //2.2.2 build-cache默认是关闭的，通过动态设置android.enableBuildCache=true打开这个功能
            //2.2.2以后引入了build-cache机制，能大幅度提高全量打包速度
            if (GradleUtils.getAndroidGradlePluginVersion().compareTo(Constants.MIN_BUILD_CACHE_ENABLED_VERSION) >= 0) {
                project.logger.error("====fastdex add dynamic property: 'android.enableBuildCache=true'")
                GradleUtils.addDynamicProperty(project,"android.enableBuildCache","true")
            }
            else {
                project.logger.error("It is recommended to use versions larger than 2.3")
            }

            project.logger.error("====fastdex add dynamic property: 'kotlin.incremental=true'")
            GradleUtils.addDynamicProperty(project,"kotlin.incremental","true")

            //最低支持2.0.0
            String minSupportVersion = "2.0.0"
            if (GradleUtils.getAndroidGradlePluginVersion().compareTo(minSupportVersion) < 0) {
                throw new GradleException("Android gradle version too old 'com.android.tools.build:gradle:${GradleUtils.getAndroidGradlePluginVersion()}', minimum support version ${minSupportVersion}")
            }

            project.tasks.create("fastdexCleanAll", FastdexCleanTask)

            def aptConfiguration = project.configurations.findByName("apt")
            def isAptEnabled = (project.plugins.hasPlugin("android-apt") || project.plugins.hasPlugin("com.neenbedankt.android-apt")) && aptConfiguration != null && !aptConfiguration.empty

            android.applicationVariants.each { variant ->
                def variantOutput = variant.outputs.first()
                def variantName = variant.name.capitalize()

                try {
                    //与instant run有冲突需要禁掉instant run
                    def instantRunTask = project.tasks.getByName("transformClassesWithInstantRunFor${variantName}")
                    if (instantRunTask) {
                        throw new GradleException(
                                "Fastdex does not support instant run mode, please trigger build"
                                        + " by assemble${variantName} or disable instant run"
                                        + " in 'File->Settings...'."
                        )
                    }
                } catch (UnknownTaskException e) {
                    // Not in instant run mode, continue.
                }

                boolean proguardEnable = variant.getVariantData().getVariantConfiguration().getBuildType().isMinifyEnabled()
                //TODO 暂时忽略开启混淆的buildType(目前的快照对比方案 无法映射java文件的类名和混淆后的class的类名)
                if (proguardEnable) {
                    String buildTypeName = variant.getBuildType().buildType.getName()
                    project.logger.error("--------------------fastdex--------------------")
                    project.logger.error("fastdex android.buildTypes.${buildTypeName}.minifyEnabled=true, just ignore")
                    project.logger.error("--------------------fastdex--------------------")
                }
                else {
                    def javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile
                    ensumeAptOutputDir(isAptEnabled, javaCompile, variant)

                    FastdexVariant fastdexVariant = new FastdexVariant(project,variant)
                    fastdexVariant.fastdexInstantRun = new FastdexInstantRun(fastdexVariant)
                    FastdexInstantRun fastdexInstantRun = fastdexVariant.fastdexInstantRun
                    if (GradleUtils.getAndroidGradlePluginVersion().compareTo("3.0") < 0) {
                        fastdexInstantRun.resourceApFile = variantOutput.processResources.packageOutputFile
                        fastdexVariant.textSymbolOutputFile = new File(variant.getVariantData().getScope().getSymbolLocation(),"R.txt")
                    }
                    else {
                        def config = variant.getVariantData().getVariantConfiguration()
                        fastdexInstantRun.resourceApFile = new File(variantOutput.processResources.getResPackageOutputFolder(),"resources-" + config.getFullName()  + ".ap_")
                        fastdexVariant.textSymbolOutputFile = variantOutput.processResources.getTextSymbolOutputFile()
                    }

                    javaCompile.doLast {
                        fastdexVariant.compiledByOriginJavac = true
                    }

                    if (FastdexUtils.isDataBindingEnabled(project)) {
                        configuration.useCustomCompile = false
                        project.logger.error("==fastdex dataBinding is enabled, disable useCustomCompile...")
                    }

                    //禁用lint任务
                    String taskName = "lintVital${variantName}"
                    try {
                        def lintTask = project.tasks.getByName(taskName)
                        lintTask.enabled = false
                    } catch (Throwable e) {

                    }

                    //创建清理指定variantName缓存的任务(用户触发)
                    FastdexCleanTask cleanTask = project.tasks.create("fastdexCleanFor${variantName}", FastdexCleanTask)
                    cleanTask.fastdexVariant = fastdexVariant

                    variantOutput.processManifest.dependsOn variant.mergeResources
                    //替换项目的Application为fastdex.runtime.FastdexApplication
                    FastdexManifestTask manifestTask = project.tasks.create("fastdexProcess${variantName}Manifest", FastdexManifestTask)
                    manifestTask.fastdexVariant = fastdexVariant

                    manifestTask.mustRunAfter variantOutput.processManifest
                    variantOutput.processResources.dependsOn manifestTask

                    //fix issue#8
                    def tinkerPatchManifestTask = getTinkerPatchManifestTask(project, variantName)
                    if (tinkerPatchManifestTask != null) {
                        manifestTask.mustRunAfter tinkerPatchManifestTask
                    }

//                    保持补丁打包时R文件中相同的节点和第一次打包时的值保持一致
//                    FastdexResourceIdTask applyResourceTask = project.tasks.create("fastdexProcess${variantName}ResourceId", FastdexResourceIdTask)
//                    applyResourceTask.fastdexVariant = fastdexVariant
//                    applyResourceTask.resDir = variantOutput.processResources.resDir
//                    applyResourceTask.mustRunAfter manifestTask
//                    variantOutput.processResources.dependsOn applyResourceTask

                    File resDir = GradleUtils.getResOutputDir(variantOutput.processResources)
                    FixPublicSymbolLogger.inject(variantOutput.processResources,resDir,fastdexVariant)

                    //这样做是为了解决第一次补丁打包时虽然资源没有发生变化但是也会执行processResources任务的问题(因为在processResources任务执行之前往输入目录里添加了public.xml)
                    variantOutput.processResources.doFirst {
                        FastdexResourceIdTask applyResourceTask = new FastdexResourceIdTask()
                        applyResourceTask.project = project
                        applyResourceTask.fastdexVariant = fastdexVariant
                        applyResourceTask.resDir = resDir
                        applyResourceTask.applyResourceId()
                    }

                    variantOutput.processResources.doLast {
                        fastdexVariant.fastdexInstantRun.onResourceChanged()
                    }

                    Task prepareTask = project.tasks.create("fastdexPrepareFor${variantName}", FastdexPrepareTask)
                    prepareTask.fastdexVariant = fastdexVariant
                    prepareTask.mustRunAfter variantOutput.processResources

                    Task generateSourcesTask = getGenerateSourcesTask(project, variantName)
                    if (generateSourcesTask != null) {
                        prepareTask.mustRunAfter generateSourcesTask
                    }

                    Task transformClassesWithJarMergingTask = getTransformClassesWithJarMergingTask(project,variantName)
                    Task transformClassesWithDexTask = getTransformClassesWithDexTask(project,variantName)
                    Task transformDexWithDexTask = getTransformDexWithDexForTask(project,variantName)
                    Task transformDexArchiveWithDexMergerTask = getTransformDexArchiveWithDexMergerTask(project,variantName)
                    Task transformClassesWithPreDexTask = getTransformClassesWithPreDexTask(project,variantName)

                    File classesDir = variant.getVariantData().getScope().getJavaOutputDir()
                    boolean hasDexCache = FastdexUtils.hasDexCache(project,variantName)

                    FastdexScanAptOutputTask scanAptOutputTask = project.tasks.create("fastdexScanAptOutputFor${variantName}", FastdexScanAptOutputTask)
                    scanAptOutputTask.fastdexVariant = fastdexVariant

                    if (configuration.useCustomCompile && hasDexCache && FileUtils.dirExists(classesDir.absolutePath)) {
                        Task customJavacTask = project.tasks.create("fastdexCustomCompile${variantName}JavaWithJavac", FastdexCustomJavacTask)
                        customJavacTask.fastdexVariant = fastdexVariant
                        customJavacTask.javaCompile = javaCompile
                        customJavacTask.javacIncrementalSafeguard = getJavacIncrementalSafeguardTask(project, variantName)
                        customJavacTask.javaPreCompile = getJavaPreCompileTask(project, variantName)
                        customJavacTask.dependsOn prepareTask

                        if (customJavacTask.javacIncrementalSafeguard != null) {
                            customJavacTask.javacIncrementalSafeguard.mustRunAfter customJavacTask
                        }
                        if (customJavacTask.javaPreCompile != null) {
                            customJavacTask.javaPreCompile.mustRunAfter customJavacTask
                        }
                        javaCompile.dependsOn customJavacTask
                        scanAptOutputTask.mustRunAfter customJavacTask
                    }
                    else {
                        javaCompile.dependsOn prepareTask
                    }

                    scanAptOutputTask.mustRunAfter javaCompile

                    //有可能成为入口的transform任务如果在scanAptOutputTask执行之后在执行
                    if (transformClassesWithJarMergingTask != null) {
                        transformClassesWithJarMergingTask.dependsOn scanAptOutputTask
                    }
                    if (transformClassesWithDexTask != null) {
                        transformClassesWithDexTask.dependsOn scanAptOutputTask
                    }
                    if (transformDexArchiveWithDexMergerTask != null) {
                        transformDexArchiveWithDexMergerTask.dependsOn scanAptOutputTask
                    }
                    if (transformDexWithDexTask != null) {
                        transformDexWithDexTask.dependsOn scanAptOutputTask
                    }
                    if (transformClassesWithPreDexTask != null) {
                        transformClassesWithPreDexTask.dependsOn scanAptOutputTask
                    }

                    Task multidexlistTask = getTransformClassesWithMultidexlistTask(project,variantName)
                    if (multidexlistTask != null) {
                        /**
                         * transformClassesWithMultidexlistFor${variantName}的作用是计算哪些类必须放在第一个dex里面，由于fastdex使用替换Application的方案隔离了项目代码的dex，
                         * 所以这个任务就没有存在的意义了，禁止掉这个任务以提高打包速度，但是transformClassesWithDexFor${variantName}会使用这个任务输出的txt文件，
                         * 所以就生成一个空文件防止报错
                         */
                        FastdexCreateMaindexlistFileTask createFileTask = project.tasks.create("fastdexCreate${variantName}MaindexlistFileTask", FastdexCreateMaindexlistFileTask)
                        createFileTask.fastdexVariant = fastdexVariant

                        multidexlistTask.dependsOn createFileTask
                        multidexlistTask.enabled = false
                    }

                    def collectMultiDexComponentsTask = getCollectMultiDexComponentsTask(project, variantName)
                    if (collectMultiDexComponentsTask != null) {
                        collectMultiDexComponentsTask.enabled = false
                    }

                    Task mergeAssetsTask = variant.mergeAssets
                    mergeAssetsTask.doLast {
                        fastdexVariant.fastdexInstantRun.onAssetsChanged()
                    }

                    FastdexPatchTask fastdexPatchTask = project.tasks.create("fastdexPatchFor${variantName}", FastdexPatchTask)
                    fastdexPatchTask.fastdexVariant = fastdexVariant

                    Task packageTask = getPackageTask(project, variantName)
                    if (transformClassesWithDexTask != null) {
                        fastdexPatchTask.mustRunAfter transformClassesWithDexTask
                    }
                    if (transformDexWithDexTask != null) {
                        fastdexPatchTask.mustRunAfter transformDexWithDexTask
                    }
                    if (transformDexArchiveWithDexMergerTask != null) {
                        fastdexPatchTask.mustRunAfter transformDexArchiveWithDexMergerTask
                    }
                    if (transformClassesWithPreDexTask != null) {
                        fastdexPatchTask.mustRunAfter transformClassesWithPreDexTask
                    }
                    fastdexPatchTask.mustRunAfter mergeAssetsTask
                    fastdexPatchTask.mustRunAfter variantOutput.processResources
                    packageTask.dependsOn fastdexPatchTask

                    if (packageTask != null) {
                        packageTask.doFirst {
                            fastdexVariant.onPrePackage()
                        }
                    }

                    FastdexInstantRunMarkTask fastdexInstantRunMarkTask = project.tasks.create("fastdexMarkFor${variantName}",FastdexInstantRunMarkTask)
                    fastdexInstantRunMarkTask.fastdexVariant = fastdexVariant

                    FastdexInstantRunTask fastdexInstantRunTask = project.tasks.create("fastdex${variantName}",FastdexInstantRunTask)
                    fastdexInstantRunTask.fastdexVariant = fastdexVariant

                    prepareTask.mustRunAfter fastdexInstantRunMarkTask

                    fastdexInstantRunTask.dependsOn variant.assemble
                    fastdexInstantRunTask.dependsOn fastdexInstantRunMarkTask

                    fastdexVariant.fastdexInstantRunTask = fastdexInstantRunTask

                    project.getGradle().getTaskGraph().addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
                        @Override
                        void graphPopulated(TaskExecutionGraph taskGraph) {
                            for (Task task : taskGraph.getAllTasks()) {
                                if (task.getProject().equals(project)
                                        && task instanceof TransformTask
                                        && task.name.startsWith("transform")
                                        && task.name.endsWith("For" + variantName)) {
                                    Transform transform = ((TransformTask) task).getTransform()

                                    println "task: " + task.name + "transform: " + transform.getClass()

                                    //如果开启了multiDex并且minSdkVersion < 21,存在transformClassesWithJarMergingFor${variantName}任务
                                    if (((transform instanceof JarMergingTransform)) && !(transform instanceof FastdexJarMergingTransform)) {
                                        if (fastdexVariant.configuration.debug) {
                                            project.logger.error("==fastdex find jarmerging transform. transform class: " + task.transform.getClass() + " . task name: " + task.name)
                                        }
                                        fastdexVariant.hasJarMergingTask = true

                                        FastdexJarMergingTransform jarMergingTransform = new FastdexJarMergingTransform(transform,task.getStreamOutputFolder(),fastdexVariant)
                                        Field field = ReflectUtils.getFieldByName(task.getClass(),'transform')
                                        field.setAccessible(true)
                                        field.set(task,jarMergingTransform)
                                    }

                                    //3.0.0以下任务名叫transformClassesWithDexFor${variantName} ; 3.0.0以上任务名叫transformDexWithDexFor${variantName}
                                    if (((transform instanceof DexTransform)) && !(transform instanceof FastdexDexTransform)) {
                                        if (fastdexVariant.configuration.debug) {
                                            project.logger.error("==fastdex find dex transform. transform class: " + task.transform.getClass() + " . task name: " + task.name)
                                        }

                                        //代理DexTransform,实现自定义的转换
                                        FastdexDexTransform dexTransform = new FastdexDexTransform(transform,task.getStreamOutputFolder(),fastdexVariant)
                                        fastdexVariant.fastdexTransform = dexTransform
                                        Field field = ReflectUtils.getFieldByName(task.getClass(),'transform')
                                        field.setAccessible(true)
                                        field.set(task,dexTransform)
                                    }

                                    //transformClassesWithDexBuilderFor${variantName},3.0.0以后才有这个任务
                                    if ((transform.getClass().getName().equals("com.android.build.gradle.internal.transforms.DexArchiveBuilderTransform") && !(transform instanceof FastdexDexBuilderTransform))) {
                                        FastdexDexBuilderTransform dexBuilderTransform = new FastdexDexBuilderTransform(transform,task.getStreamOutputFolder(),fastdexVariant)
                                        fastdexVariant.dexBuilderOutputFolder = task.getStreamOutputFolder()

                                        Field field = ReflectUtils.getFieldByName(task.getClass(),'transform')
                                        field.setAccessible(true)
                                        field.set(task,dexBuilderTransform)
                                    }

                                    //transformDexArchiveWithDexMergerFor${variantName},3.0.0以后才有这个任务
                                    if ((transform.getClass().getName().equals("com.android.build.gradle.internal.transforms.DexMergerTransform") && !(transform instanceof FastdexDexBuilderTransform))) {
                                        FastdexDexMergerTransform dexMergerTransform = new FastdexDexMergerTransform(transform,task.getStreamOutputFolder(),fastdexVariant)
                                        fastdexVariant.dexMergerOutputFolder = task.getStreamOutputFolder()

                                        Field field = ReflectUtils.getFieldByName(task.getClass(),'transform')
                                        field.setAccessible(true)
                                        field.set(task,dexMergerTransform)
                                    }

                                    //transformClassesWithPreDexFor{variantName},3.0.0以后才有这个任务
                                    if ((transform.getClass().getName().equals("com.android.build.gradle.internal.transforms.PreDexTransform") && !(transform instanceof FastdexPreDexTransform))) {
                                        FastdexPreDexTransform preDexTransform = new FastdexPreDexTransform(transform,task.getStreamOutputFolder(),fastdexVariant)
                                        fastdexVariant.preDexOutputFolder = task.getStreamOutputFolder()
                                        fastdexVariant.hasPreDexTask = true

                                        Field field = ReflectUtils.getFieldByName(task.getClass(),'transform')
                                        field.setAccessible(true)
                                        field.set(task,preDexTransform)
                                    }
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    def ensumeAptOutputDir(boolean isAptEnabled, Object javaCompile, ApplicationVariant variant) {
        //2.2.0之前的版本java编译任务没有指定-s参数，需要自己指定到apt目录
        if (isAptEnabled) {
            return
        }
        if (GradleUtils.getAndroidGradlePluginVersion().compareTo("2.2") >= 0) {
            return
        }

        def aptOutputDir = new File(variant.getVariantData().getScope().getGlobalScope().getGeneratedDir(), "/source/apt")
        def aptOutput = new File(aptOutputDir, variant.dirName)

        if (variant.variantData.extraGeneratedSourceFolders == null || !variant.variantData.extraGeneratedSourceFolders.contains(aptOutput)) {
            variant.addJavaSourceFoldersToModel(aptOutput)
        }

        javaCompile.doFirst {
            if (!aptOutput.exists()) {
                aptOutput.mkdirs()
            }
        }

        if (javaCompile.options.compilerArgs == null) {
            javaCompile.options.compilerArgs = new ArrayList<>()
        }

        def compilerArgs = new ArrayList<>()
        compilerArgs.addAll(javaCompile.options.compilerArgs)

        boolean discoveryAptOutput = false
        def originAptOutput = null

        for (Object obj : compilerArgs) {
            if (discoveryAptOutput) {
                originAptOutput = obj
                break
            }
            if ("-s".equals(obj)) {
                discoveryAptOutput = true
            }
        }

        if (discoveryAptOutput) {
            compilerArgs.remove("-s")
            compilerArgs.remove(originAptOutput)
        }
        compilerArgs.add(0,"-s")
        compilerArgs.add(1,aptOutput)

        javaCompile.options.compilerArgs.clear()
        javaCompile.options.compilerArgs.addAll(compilerArgs)
    }

    //禁掉aapt2
    def reflectAapt2Flag(Project project) {
        try {
            def booleanOptClazz = Class.forName('com.android.build.gradle.options.BooleanOption')
            def enableAAPT2Field = booleanOptClazz.getDeclaredField('ENABLE_AAPT2')
            enableAAPT2Field.setAccessible(true)
            def enableAAPT2EnumObj = enableAAPT2Field.get(null)
            def defValField = enableAAPT2EnumObj.getClass().getDeclaredField('defaultValue')
            defValField.setAccessible(true)
            defValField.set(enableAAPT2EnumObj, false)
        } catch (Throwable thr) {
            project.logger.error("relectAapt2Flag error: ${thr.getMessage()}.")
        }
    }

    //禁止掉dex archive任务
    def reflectDexArchive(Project project) {
        try {
            def booleanOptClazz = Class.forName('com.android.build.gradle.options.BooleanOption')

            def enableDexArchiveField = booleanOptClazz.getDeclaredField('ENABLE_DEX_ARCHIVE')
            enableDexArchiveField.setAccessible(true)
            def enableDexArchiveObj = enableDexArchiveField.get(null)
            def defValField = enableDexArchiveObj.getClass().getDeclaredField('defaultValue')
            defValField.setAccessible(true)
            defValField.set(enableDexArchiveObj, false)
        } catch (Throwable thr) {
            project.logger.error("reflectDexArchive error: ${thr.getMessage()}.")
        }
    }

    Task getPackageTask(Project project, String variantName) {
        String taskName = "package${variantName}"
        try {
            return  project.tasks.getByName(taskName)
        } catch (Throwable e) {
            return null
        }
    }

    Task getJavacIncrementalSafeguardTask(Project project, String variantName) {
        String taskName = "incremental${variantName}JavaCompilationSafeguard"
        try {
            return  project.tasks.getByName(taskName)
        } catch (Throwable e) {
            return null
        }
    }

    Task getJavaPreCompileTask(Project project, String variantName) {
        String taskName = "javaPreCompile${variantName}"
        try {
            return  project.tasks.getByName(taskName)
        } catch (Throwable e) {
            return null
        }
    }

    Task getGenerateSourcesTask(Project project, String variantName) {
        String taskName = "generate${variantName}Sources"
        try {
            return  project.tasks.getByName(taskName)
        } catch (Throwable e) {
            return null
        }
    }

    Task getTinkerPatchManifestTask(Project project, String variantName) {
        String taskName = "tinkerpatchSupportProcess${variantName}Manifest"
        try {
            return project.tasks.getByName(taskName)
        } catch (Throwable e) {
            return null
        }
    }

    Task getTransformClassesWithMultidexlistTask(Project project, String variantName) {
        String taskName = "transformClassesWithMultidexlistFor${variantName}"
        try {
            return project.tasks.getByName(taskName)
        } catch (Throwable e) {
            //fix issue #1 如果没有开启multidex会报错
            return null
        }
    }

    Task getTransformClassesWithDexTask(Project project, String variantName) {
        String taskName = "transformClassesWithDexFor${variantName}"
        try {
            return project.tasks.findByName(taskName)
        } catch (Throwable e) {
            return null
        }
    }

    Task getTransformClassesWithPreDexTask(Project project, String variantName) {
        String taskName = "transformClassesWithPreDexFor${variantName}"
        try {
            return project.tasks.findByName(taskName)
        } catch (Throwable e) {
            return null
        }
    }

    Task getTransformClassesWithJarMergingTask(Project project, String variantName) {
        String taskName = "transformClassesWithJarMergingFor${variantName}"
        try {
            return project.tasks.findByName(taskName)
        } catch (Throwable e) {
            return null
        }
    }

    Task getTransformDexWithDexForTask(Project project, String variantName) {
        String taskName = "transformDexWithDexFor${variantName}"
        try {
            return project.tasks.findByName(taskName)
        } catch (Throwable e) {
            return null
        }
    }

    Task getTransformDexArchiveWithDexMergerTask(Project project, String variantName) {
        String taskName = "transformDexArchiveWithDexMergerFor${variantName}"
        try {
            return project.tasks.findByName(taskName)
        } catch (Throwable e) {
            return null
        }
    }

    Task getCollectMultiDexComponentsTask(Project project, String variantName) {
        String taskName = "collect${variantName}MultiDexComponents"
        try {
            return project.tasks.findByName(taskName)
        } catch (Throwable e) {
            return null
        }
    }
}