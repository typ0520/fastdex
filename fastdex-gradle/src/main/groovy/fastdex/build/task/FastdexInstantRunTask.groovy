package fastdex.build.task

import fastdex.build.util.FastdexInstantRun
import fastdex.build.util.FastdexRuntimeException
import fastdex.build.variant.FastdexVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by tong on 17/3/12.
 */
public class FastdexInstantRunTask extends DefaultTask {
    FastdexVariant fastdexVariant

    FastdexInstantRunTask() {
        group = 'fastdex'
    }

    @TaskAction
    void instantRun() {
        FastdexInstantRun fastdexInstantRun = fastdexVariant.fastdexInstantRun

        if (!fastdexInstantRun.isInstallApk()) {
            return
        }

        def targetVariant = fastdexVariant.androidVariant
        project.logger.error("==fastdex normal run ${fastdexVariant.variantName}")
        //安装app
        File apkFile = targetVariant.outputs.first().getOutputFile()
        project.logger.error("adb install -r ${apkFile}")

        try {
            fastdexInstantRun.device.installPackage(apkFile.absolutePath,true)
        } catch (Throwable e) {
            throw new FastdexRuntimeException(e)
        }
        fastdexInstantRun.startBootActivity()
    }
}
