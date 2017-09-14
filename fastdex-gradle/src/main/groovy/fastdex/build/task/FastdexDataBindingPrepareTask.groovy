package fastdex.build.task

import fastdex.build.variant.FastdexVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 开启DataBinding准备上下文环境
 * Created by tong on 17/9/14.
 */
public class FastdexDataBindingPrepareTask extends DefaultTask {
    FastdexVariant fastdexVariant

    FastdexDataBindingPrepareTask() {
        group = 'fastdex'
    }

    @TaskAction
    void prepareContext() {
        fastdexVariant.projectSnapshoot.prepareEnv()
        fastdexVariant.fastdexInstantRun.onFastdexPrepare()
    }
}
