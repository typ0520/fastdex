package fastdex.build.task

import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by tong on 17/12/12.
 */
class FastdexIgnoreTask extends DefaultTask {
    ApplicationVariant androidVariant
    boolean proguardEnable
    boolean ignoreBuildType

    FastdexIgnoreTask() {
        group = 'fastdex'
    }

    @TaskAction
    def instantRun() {
        String buildTypeName = androidVariant.getBuildType().buildType.getName()
        project.logger.error("--------------------fastdex--------------------")
        if (ignoreBuildType) {
            project.logger.error("onlyHookDebug = true, build-type = ${buildTypeName}, just ignore")
        }
        else if (proguardEnable) {
            project.logger.error("fastdex android.buildTypes.${buildTypeName}.minifyEnabled=true, just ignore")
        }
        project.logger.error("--------------------fastdex--------------------")
    }
}
