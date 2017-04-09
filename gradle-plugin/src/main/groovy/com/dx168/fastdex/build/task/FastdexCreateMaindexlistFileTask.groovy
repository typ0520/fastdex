package com.dx168.fastdex.build.task

import com.dx168.fastdex.build.util.FileUtils
import com.dx168.fastdex.build.variant.FastdexVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * transformClassesWithMultidexlistFor${variantName}的作用是计算哪些类必须放在第一个dex里面，由于fastdex使用替换Application的方案隔离了项目代码的dex，
 * 所以这个任务就没有存在的意义了，禁止掉这个任务以提高打包速度，但是transformClassesWithDexFor${variantName}会使用这个任务输出的txt文件，所以需要生成一个空文件防止报错
 * Created by tong on 17/3/12.
 */
public class FastdexCreateMaindexlistFileTask extends DefaultTask {
    FastdexVariant fastdexVariant

    FastdexCreateMaindexlistFileTask() {
        group = 'fastdex'
    }

    @TaskAction
    void createFile() {
        if (fastdexVariant.androidVariant != null) {
            File maindexlistFile = fastdexVariant.androidVariant.getVariantData().getScope().getMainDexListFile()
            File parentFile = maindexlistFile.getParentFile()
            FileUtils.ensumeDir(parentFile)

            if (!FileUtils.isLegalFile(maindexlistFile)) {
                maindexlistFile.createNewFile()
            }
        }
    }
}
