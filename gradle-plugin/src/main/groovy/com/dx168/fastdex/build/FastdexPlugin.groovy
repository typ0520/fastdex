package com.dx168.fastdex.build

import com.android.build.api.transform.Transform
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.transforms.DexTransform
import com.android.build.gradle.internal.transforms.JarMergingTransform
import com.dx168.fastdex.build.task.FastdexCleanTask
import com.dx168.fastdex.build.task.FastdexCreateMaindexlistFileTask
import com.dx168.fastdex.build.task.FastdexManifestTask
import com.dx168.fastdex.build.task.FastdexResourceIdTask
import com.dx168.fastdex.build.transform.FastdexJarMergingTransform
import com.dx168.fastdex.build.util.BuildTimeListener
import com.dx168.fastdex.build.util.Constant
import com.dx168.fastdex.build.util.GradleUtils
import com.dx168.fastdex.build.variant.FastdexVariant
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener
import java.lang.reflect.Field
import com.dx168.fastdex.build.transform.FastdexTransform
import com.dx168.fastdex.build.extension.FastdexExtension
import com.dx168.fastdex.build.task.FastdexCustomJavacTask

/**
 * 注册相应节点的任务
 * Created by tong on 17/10/3.
 */
class FastdexPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.gradle.addListener(new BuildTimeListener())
        project.extensions.create('fastdex', FastdexExtension)

        project.afterEvaluate {
            def configuration = project.fastdex

            if (!configuration.fastdexEnable) {
                project.logger.error("====fastdex tasks are disabled.====")
                return
            }

            if (!project.plugins.hasPlugin('com.android.application')) {
                throw new GradleException('generateTinkerApk: Android Application plugin required')
            }

            //最低支持2.0.0
            String androidGradlePluginVersion = GradleUtils.ANDROID_GRADLE_PLUGIN_VERSION
            if (androidGradlePluginVersion.compareTo(Constant.MIN_SUPPORT_ANDROID_GRADLE_VERSION) < 0) {
                throw new GradleException("Your version too old 'com.android.tools.build:gradle:${androidGradlePluginVersion}', minimum support version 2.0.0")
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

            android.applicationVariants.all { variant ->
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

                //创建清理指定variantName缓存的任务(用户触发)
                FastdexCleanTask cleanTask = project.tasks.create("fastdexCleanFor${variantName}", FastdexCleanTask)
                cleanTask.fastdexVariant = fastdexVariant

                boolean proguardEnable = variant.getBuildType().buildType.minifyEnabled
                //TODO 暂时忽略开启混淆的buildType(目前的快照对比方案 无法映射java文件的类名和混淆后的class的类名)
                if (proguardEnable) {
                    String buildTypeName = variant.getBuildType().buildType.getName()
                    project.logger.error("====================fastdex====================")
                    project.logger.error("==fastdex android.buildTypes.${buildTypeName}.minifyEnabled=true, just ignore")
                    project.logger.error("====================fastdex====================")
                }
                else {
                    Task compileTask = project.tasks.getByName("compile${variantName}JavaWithJavac")
                    Task customJavacTask = project.tasks.create("fastdexCustomCompile${variantName}JavaWithJavac", FastdexCustomJavacTask)
                    customJavacTask.fastdexVariant = fastdexVariant
                    customJavacTask.compileTask = compileTask
                    compileTask.dependsOn customJavacTask

                    Task generateSourcesTask = getGenerateSourcesTask(project,variantName)
                    if (generateSourcesTask != null) {
                        compileTask.mustRunAfter generateSourcesTask
                    }
                    else {
                        compileTask.mustRunAfter variantOutput.processResources
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

                    project.getGradle().getTaskGraph().addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
                        @Override
                        public void graphPopulated(TaskExecutionGraph taskGraph) {
                            for (Task task : taskGraph.getAllTasks()) {
                                if (task.getProject().equals(project)
                                        && task instanceof TransformTask
                                        && task.name.toLowerCase().contains(variant.name.toLowerCase())) {

                                    Transform transform = ((TransformTask) task).getTransform()
                                    //如果开启了multiDexEnabled true,存在transformClassesWithJarMergingFor${variantName}任务
                                    if ((((transform instanceof JarMergingTransform)) && !(transform instanceof FastdexJarMergingTransform))) {
                                        project.logger.error("==fastdex find jarmerging transform. transform class: " + task.transform.getClass() + " . task name: " + task.name)

                                        FastdexJarMergingTransform jarMergingTransform = new FastdexJarMergingTransform(transform,fastdexVariant)
                                        Field field = getFieldByName(task.getClass(),'transform')
                                        field.setAccessible(true)
                                        field.set(task,jarMergingTransform)
                                    }

                                    if ((((transform instanceof DexTransform)) && !(transform instanceof FastdexTransform))) {
                                        project.logger.error("==fastdex find dex transform. transform class: " + task.transform.getClass() + " . task name: " + task.name)

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

    Task getTinkerPatchManifestTask(Project project, String variantName) {
        String tinkerPatchManifestTaskName = "tinkerpatchSupportProcess${variantName}Manifest"
        try {
            return  project.tasks.getByName(tinkerPatchManifestTaskName)
        } catch (Throwable e) {
            return null
        }
    }

    Task getGenerateSourcesTask(Project project, String variantName) {
        String generateSourcesTaskName = "generate${variantName}Sources"
        try {
            return  project.tasks.getByName(generateSourcesTaskName)
        } catch (Throwable e) {
            return null
        }
    }

    Task getTransformClassesWithMultidexlistTask(Project project, String variantName) {
        String transformClassesWithMultidexlistTaskName = "transformClassesWithMultidexlistFor${variantName}"
        try {
            return project.tasks.getByName(transformClassesWithMultidexlistTaskName)
        } catch (Throwable e) {
            //fix issue #1 如果没有开启multidex会报错
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