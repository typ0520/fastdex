package com.dx168.fastdex.build.task

import com.dx168.fastdex.build.util.FastdexUtils
import com.dx168.fastdex.build.variant.FastdexVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 清空指定variantName的缓存，如果variantName == null清空所有缓存
 * Created by tong on 17/3/12.
 */
public class FastdexCleanTask extends DefaultTask {
    FastdexVariant fastdexVariant

    FastdexCleanTask() {
        group = 'fastdex'
    }

    @TaskAction
    void clean() {
        if (fastdexVariant == null) {
            FastdexUtils.cleanAllCache(project)
        }
        else {
            FastdexUtils.cleanCache(project,variantName)
        }
    }
}
