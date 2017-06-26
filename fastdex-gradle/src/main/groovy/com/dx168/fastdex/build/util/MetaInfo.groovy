package com.dx168.fastdex.build.util

import fastdex.common.utils.SerializeUtils
import com.dx168.fastdex.build.variant.FastdexVariant
import com.google.gson.Gson
import fastdex.common.utils.FileUtils
import org.gradle.api.Project;

/**
 * Created by tong on 17/4/18.
 */
public class MetaInfo {
    /**
     * 全量编译时的工程路径
     */
    public String projectPath

    public String rootProjectPath

    public String fastdexVersion
    /**
     * 全量编译完成后输出的dex个数
     */
    public int dexCount

    /**
     * 全量编译完成的时间
     */
    public long buildMillis

    public String variantName

    public int mergedDexVersion

    public int patchDexVersion


    /**
     * 是否移动了工程目录
     * @param project
     * @return
     */
    public boolean isRootProjectDirChanged(String curRootProjectPath) {
        return !curRootProjectPath.equals(rootProjectPath)
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

    @Override
    public String toString() {
        return "MetaInfo{" +
                "buildMillis=" + buildMillis +
                ", variantName='" + variantName + '\'' +
                '}';
    }
}
