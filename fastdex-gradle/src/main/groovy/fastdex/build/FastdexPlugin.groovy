package fastdex.build

import com.android.build.api.transform.Transform
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.transforms.DexTransform
import com.android.build.gradle.internal.transforms.JarMergingTransform
import fastdex.build.task.FastdexCleanTask
import fastdex.build.task.FastdexCreateMaindexlistFileTask
import fastdex.build.task.FastdexInstantRunMarkTask
import fastdex.build.task.FastdexInstantRunTask
import fastdex.build.task.FastdexManifestTask
import fastdex.build.task.FastdexPatchTask
import fastdex.build.task.FastdexResourceIdTask
import fastdex.build.transform.FastdexJarMergingTransform
import fastdex.build.util.FastdexBuildListener
import fastdex.build.util.Constants
import fastdex.build.util.FastdexInstantRun
import fastdex.build.util.FastdexUtils
import fastdex.build.util.GradleUtils
import fastdex.build.variant.FastdexVariant
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener
import java.lang.reflect.Field
import fastdex.build.transform.FastdexTransform
import fastdex.build.extension.FastdexExtension
import fastdex.build.task.FastdexPrepareTask
import fastdex.build.task.FastdexCustomJavacTask

/**
 * 注册相应节点的任务
 * Created by tong on 17/10/3.
 */
class FastdexPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.extensions.create('fastdex', FastdexExtension)

        FastdexBuildListener.addByProject(project)
        project.afterEvaluate {
            def configuration = project.fastdex
            if (!configuration.fastdexEnable) {
                project.logger.error("====fastdex tasks are disabled.====")
                return
            }
            if (!project.plugins.hasPlugin('com.android.application')) {
                throw new GradleException('generateTinkerApk: Android Application plugin required')
            }

            if (FastdexUtils.isDataBindingEnabled(project)) {
                //最低支持2.2.0
                String androidGradlePluginVersion = GradleUtils.ANDROID_GRADLE_PLUGIN_VERSION
                if (androidGradlePluginVersion.compareTo("2.2") < 0) {
                    throw new GradleException("DataBinding enabled, minimum support version 2.2.0")
                }
            }
            else {
                //最低支持2.0.0
                String androidGradlePluginVersion = GradleUtils.ANDROID_GRADLE_PLUGIN_VERSION
                if (androidGradlePluginVersion.compareTo(Constants.MIN_SUPPORT_ANDROID_GRADLE_VERSION) < 0) {
                    throw new GradleException("Your version too old 'com.android.tools.build:gradle:${androidGradlePluginVersion}', minimum support version 2.0.0")
                }
            }

            def android = project.extensions.android
            //open jumboMode
            android.dexOptions.jumboMode = true
            //close preDexLibraries
            try {
                android.dexOptions.preDexLibraries = false
            } catch (Throwable e) {
                //no preDexLibraries field, just continue
            }

            project.tasks.create("fastdexCleanAll", FastdexCleanTask)

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

                FastdexVariant fastdexVariant = new FastdexVariant(project,variant)
                fastdexVariant.fastdexInstantRun = new FastdexInstantRun(fastdexVariant)

                FastdexInstantRun fastdexInstantRun = fastdexVariant.fastdexInstantRun
                fastdexInstantRun.resourceApFile = variantOutput.processResources.packageOutputFile
                fastdexInstantRun.resDir = variantOutput.processResources.resDir


                boolean proguardEnable = variant.getBuildType().buildType.minifyEnabled
                //TODO 暂时忽略开启混淆的buildType(目前的快照对比方案 无法映射java文件的类名和混淆后的class的类名)
                if (proguardEnable) {
                    String buildTypeName = variant.getBuildType().buildType.getName()
                    project.logger.error("--------------------fastdex--------------------")
                    project.logger.error("fastdex android.buildTypes.${buildTypeName}.minifyEnabled=true, just ignore")
                    project.logger.error("--------------------fastdex--------------------")
                }
                else {
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


                    //TODO change api
                    variantOutput.processManifest.dependsOn getMergeDebugResources(project,variantName)
                    //variantOutput.processManifest.dependsOn variant.getVariantData().getScope().getMergeResourcesTask()
                    //替换项目的Application为com.dx168.fastdex.runtime.FastdexApplication
                    FastdexManifestTask manifestTask = project.tasks.create("fastdexProcess${variantName}Manifest", FastdexManifestTask)
                    manifestTask.fastdexVariant = fastdexVariant
                    manifestTask.mustRunAfter variantOutput.processManifest
                    variantOutput.processResources.dependsOn manifestTask

                    //fix issue#8
                    def tinkerPatchManifestTask = getTinkerPatchManifestTask(project, variantName)
                    if (tinkerPatchManifestTask != null) {
                        manifestTask.mustRunAfter tinkerPatchManifestTask
                    }

                    //保持补丁打包时R文件中相同的节点和第一次打包时的值保持一致
                    FastdexResourceIdTask applyResourceTask = project.tasks.create("fastdexProcess${variantName}ResourceId", FastdexResourceIdTask)
                    applyResourceTask.fastdexVariant = fastdexVariant
                    applyResourceTask.resDir = variantOutput.processResources.resDir
                    //let applyResourceTask run after manifestTask
                    applyResourceTask.mustRunAfter manifestTask
                    variantOutput.processResources.dependsOn applyResourceTask

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

                    Task transformClassesWithDex = getTransformClassesWithDex(project,variantName)
                    if (FastdexUtils.isDataBindingEnabled(project)) {
                        transformClassesWithDex.dependsOn prepareTask
                        prepareTask.mustRunAfter variant.javaCompile
                    }
                    else {
                        if (configuration.useCustomCompile) {
                            Task customJavacTask = project.tasks.create("fastdexCustomCompile${variantName}JavaWithJavac", FastdexCustomJavacTask)
                            customJavacTask.fastdexVariant = fastdexVariant
                            customJavacTask.javaCompile = variant.javaCompile
                            customJavacTask.javacIncrementalSafeguard = getJavacIncrementalSafeguardTask(project, variantName)

                            customJavacTask.dependsOn prepareTask
                            variant.javaCompile.dependsOn customJavacTask
                        }
                        else {
                            variant.javaCompile.dependsOn prepareTask
                        }
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

                    Task mergeAssetsTask = getMergeAssetsTask(project, variantName)
                    mergeAssetsTask.doLast {
                        fastdexVariant.fastdexInstantRun.onAssetsChanged()
                    }

                    FastdexPatchTask fastdexPatchTask = project.tasks.create("fastdexPatchFor${variantName}", FastdexPatchTask)
                    fastdexPatchTask.fastdexVariant = fastdexVariant

                    Task packageTask = getPackageTask(project, variantName)
                    fastdexPatchTask.mustRunAfter transformClassesWithDex
                    fastdexPatchTask.mustRunAfter mergeAssetsTask
                    packageTask.dependsOn fastdexPatchTask


                    if (packageTask != null) {
                        project.logger.error("==fastdex find package task: " + packageTask.name)

                        packageTask.doFirst {
                            fastdexVariant.copyMetaInfo2Assets()
                        }
                    }

                    FastdexInstantRunTask fastdexInstantRunTask = project.tasks.create("fastdex${variantName}",FastdexInstantRunTask)
                    fastdexInstantRunTask.fastdexVariant = fastdexVariant

                    FastdexInstantRunMarkTask fastdexInstantRunMarkTask = project.tasks.create("fastdexMarkFor${variantName}",FastdexInstantRunMarkTask)
                    fastdexInstantRunMarkTask.fastdexVariant = fastdexVariant
                    //fastdexInstantRunMarkTask.mustRunAfter variantOutput.processManifest

                    prepareTask.mustRunAfter fastdexInstantRunMarkTask

                    fastdexInstantRunTask.dependsOn variant.assemble
                    fastdexInstantRunTask.dependsOn fastdexInstantRunMarkTask

                    fastdexVariant.fastdexInstantRunTask = fastdexInstantRunTask

                    project.getGradle().getTaskGraph().addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
                        @Override
                        public void graphPopulated(TaskExecutionGraph taskGraph) {
                            for (Task task : taskGraph.getAllTasks()) {
                                if (task.getProject().equals(project)
                                        && task instanceof TransformTask
                                        //fix #
                                        && task.name.endsWith("For" + variantName)) {
                                    Transform transform = ((TransformTask) task).getTransform()
                                    //如果开启了multiDexEnabled true,存在transformClassesWithJarMergingFor${variantName}任务
                                    if ((((transform instanceof JarMergingTransform)) && !(transform instanceof FastdexJarMergingTransform))) {
                                        fastdexVariant.hasJarMergingTask = true
                                        if (fastdexVariant.configuration.debug) {
                                            project.logger.error("==fastdex find jarmerging transform. transform class: " + task.transform.getClass() + " . task name: " + task.name)
                                        }

                                        FastdexJarMergingTransform jarMergingTransform = new FastdexJarMergingTransform(transform,fastdexVariant)
                                        Field field = getFieldByName(task.getClass(),'transform')
                                        field.setAccessible(true)
                                        field.set(task,jarMergingTransform)
                                    }

                                    if ((((transform instanceof DexTransform)) && !(transform instanceof FastdexTransform))) {
                                        if (fastdexVariant.configuration.debug) {
                                            project.logger.error("==fastdex find dex transform. transform class: " + task.transform.getClass() + " . task name: " + task.name)
                                        }

                                        //代理DexTransform,实现自定义的转换
                                        FastdexTransform fastdexTransform = new FastdexTransform(transform,fastdexVariant)
                                        Field field = getFieldByName(task.getClass(),'transform')
                                        field.setAccessible(true)
                                        field.set(task,fastdexTransform)
                                    }
                                }
                            }
                        }
                    });

                }
            }
        }
    }

    Task getMergeAssetsTask(Project project, String variantName) {
        String taskName = "merge${variantName}Assets"
        try {
            return  project.tasks.getByName(taskName)
        } catch (Throwable e) {
            return null
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

    Task getMergeDebugResources(Project project, String variantName) {
        String taskName = "merge${variantName}Resources"
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

    Task getTransformClassesWithDex(Project project, String variantName) {
        String taskName = "transformClassesWithDexFor${variantName}"
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

    Field getFieldByName(Class<?> aClass, String name) {
        Class<?> currentClass = aClass;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // ignored.
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }
}