package com.dx168.fastdex.build.util

import com.dx168.fastdex.build.snapshoot.utils.SerializeUtils
import com.dx168.fastdex.build.variant.FastdexVariant
import com.google.gson.Gson
import org.gradle.api.Project;

/**
 * Created by tong on 17/4/18.
 */
public class MetaInfo {
    /**
     * 全量编译时的工程路径
     */
    public String projectPath
    /**
     * 全量编译完成后输出的dex个数
     */
    public int dexCount

    /**
     * 全量编译完成的时间
     */
    public int buildMillis

    /**
     * 缓存对应的工程路径是否与
     * @param project
     * @return
     */
    public boolean isProjectDirChanged(Project project) {
        return !project.projectDir.absolutePath.equals(projectPath)
    }

    public void save(FastdexVariant fastdexVariant) {
        File metaInfoFile = FastdexUtils.getMetaInfoFile(fastdexVariant.project,fastdexVariant.variantName)
        SerializeUtils.serializeTo(new FileOutputStream(metaInfoFile),this)
    }

    public static MetaInfo load(Project project,String variantName) {
        File metaInfoFile = FastdexUtils.getMetaInfoFile(project,variantName)
        try {
            return new Gson().fromJson(new String(FileUtils.readContents(metaInfoFile)),MetaInfo.class)
        } catch (Throwable e) {
            e.printStackTrace()
        }
    }
}
