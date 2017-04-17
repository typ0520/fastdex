package com.dx168.fastdex.build.util

import org.gradle.BuildListener;
import org.gradle.BuildResult
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionListener;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.tasks.TaskState
import org.gradle.util.Clock;
import com.dx168.fastdex.build.FastdexPlugin

/**
 * Created by tong on 17/3/12.
 */
class FastdexBuildListener implements TaskExecutionListener, BuildListener {
    private Clock clock
    private times = []
    private Project project

    FastdexBuildListener(Project project) {
        this.project = project
    }

    @Override
    void beforeExecute(Task task) {
        clock = new org.gradle.util.Clock()
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        def ms = clock.timeInMs
        times.add([ms, task.path])

        //task.project.logger.warn "${task.path} spend ${ms}ms"
    }

    @Override
    void buildFinished(BuildResult result) {
        if (result.failure == null) {
            println "Task spend time:"
            for (time in times) {
                if (time[0] >= 50) {
                    printf "%7sms  %s\n", time
                }
            }
        }
        else {
            if (project == null || !project.plugins.hasPlugin("com.android.application")) {
                return
            }

            Throwable cause = getRootThowable(result.failure)
            if (cause == null) {
                return
            }

            StackTraceElement[] stackTrace = cause.getStackTrace()
            if (stackTrace == null || stackTrace.length == 0) {
                return
            }

            StackTraceElement stackTraceElement = stackTrace[0]
            if (stackTraceElement == null) {
                return
            }

            if (stackTraceElement.toString().contains(FastdexPlugin.class.getPackage().getName())) {
                File errorLogFile = new File(FastdexUtils.getBuildDir(project),Constant.ERROR_REPORT_FILENAME)

                println("\n===========================fastdex error report===========================")
                ByteArrayOutputStream bos = new ByteArrayOutputStream()
                result.failure.printStackTrace(new PrintStream(bos))

                String splitStr = "\n\n"
                StringBuilder report = new StringBuilder()
                //让android studio的Messages窗口显示打开Gradle Console的提示
                report.append("Caused by: ----------------------------------fastdex---------------------------------\n")
                report.append("Caused by: Open the Gradle Console in the lower right corner to view the build error report\n")
                report.append("Caused by: ${errorLogFile}\n")
                report.append("Caused by: ----------------------------------fastdex---------------------------------${splitStr}")
                report.append("${new String(bos.toByteArray())}\n")
                report.append("OS                        : ${getOsName()}\n")
                report.append("android_build_version     : ${GradleUtils.ANDROID_GRADLE_PLUGIN_VERSION}\n")
                report.append("gradle_version            : ${project.gradle.gradleVersion}\n")
                report.append("buildToolsVersion         : ${project.android.getBuildToolsVersion()}\n")
                report.append("compileSdkVersion         : ${project.android.getCompileSdkVersion()}\n")
                report.append("default minSdkVersion     : ${project.android.defaultConfig.minSdkVersion.getApiString()}\n")
                report.append("default targetSdkVersion  : ${project.android.defaultConfig.targetSdkVersion.getApiString()}\n")
                report.append("default multiDexEnabled   : ${project.android.defaultConfig.multiDexEnabled}\n\n")
                report.append("fastdex build exception, welcome to submit issue to us: https://github.com/typ0520/fastdex/issues")
                System.err.println(report.toString())
                System.err.println("${errorLogFile}")

                int idx = report.indexOf(splitStr)
                String content = report.toString()
                if (idx != -1 && (idx + splitStr.length()) < content.length()) {
                    content = content.substring(idx + splitStr.length())
                }
                FileUtils.write2file(content.getBytes(),errorLogFile)
                println("\n===========================fastdex error report===========================")
            }
        }
    }

    @Override
    void buildStarted(Gradle gradle) {}

    @Override
    void projectsEvaluated(Gradle gradle) {}

    @Override
    void projectsLoaded(Gradle gradle) {}

    @Override
    void settingsEvaluated(Settings settings) {}

    String getOsName() {
        try {
            return System.getProperty("os.name").toLowerCase(Locale.ENGLISH)
        } catch (Throwable e) {

        }
        return ""
    }

    Throwable getRootThowable(Throwable throwable) {
        return throwable.cause != null ? getRootThowable(throwable.cause) : throwable
    }

    public static void addByProject(Project pro) {
        FastdexBuildListener listener = new FastdexBuildListener(pro)
        pro.gradle.addListener(listener)
    }
}