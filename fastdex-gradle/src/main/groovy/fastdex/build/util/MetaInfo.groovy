package fastdex.build.util

import fastdex.common.utils.SerializeUtils
import fastdex.build.variant.FastdexVariant
import com.google.gson.Gson
import fastdex.common.utils.FileUtils
import org.gradle.api.Project

/**
 * Created by tong on 17/4/18.
 */
class MetaInfo {
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

    public int resourcesVersion

    public boolean active = true

    /**
     * 是否移动了工程目录
     * @param project
     * @return
     */
    def isRootProjectDirChanged(String curRootProjectPath) {
        return !curRootProjectPath.equals(rootProjectPath)
    }

    def save(FastdexVariant fastdexVariant) {
        File metaInfoFile = FastdexUtils.getMetaInfoFile(fastdexVariant.project,fastdexVariant.variantName)
        SerializeUtils.serializeTo(new FileOutputStream(metaInfoFile),this)
    }

    @Override
    String toString() {
        return "MetaInfo{" +
                "buildMillis=" + buildMillis +
                ", variantName='" + variantName + '\'' +
                ", mergedDexVersion=" + mergedDexVersion +
                ", patchDexVersion=" + patchDexVersion +
                ", resourcesVersion=" + resourcesVersion +
                '}'
    }

    static MetaInfo load(Project project,String variantName) {
        File metaInfoFile = FastdexUtils.getMetaInfoFile(project,variantName)
        return new Gson().fromJson(new String(FileUtils.readContents(metaInfoFile)),MetaInfo.class)
    }
}
