package com.dx168.fastdex.build.task

import com.dx168.fastdex.build.variant.FastdexVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 准备上下文环境
 * Created by tong on 17/4/18.
 */
public class FastdexPrepareTask extends DefaultTask {
    FastdexVariant fastdexVariant

    FastdexPrepareTask() {
        group = 'fastdex'
    }

    @TaskAction
    void prepareContext() {
        fastdexVariant.prepareEnv()
    }
}
