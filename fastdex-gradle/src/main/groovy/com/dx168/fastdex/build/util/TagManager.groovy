package com.dx168.fastdex.build.util

import org.gradle.api.Project;

/**
 * Created by tong on 17/3/14.
 */
public class TagManager {
    final Project project
    final String variantName

    TagManager(Project project, String variantName) {
        this.project = project
        this.variantName = variantName
    }

    /**
     * 是否有某个标志
     * @param tag
     * @return
     */
    public boolean hasTag(String tag) {
        if (tag == null || tag.length() == 0) {
            return false
        }
        File file = getTagFile(tag)
        return file.exists() && file.isFile()
    }

    /**
     * 保存标记
     * @param tag
     */
    public boolean saveTag(String tag) {
        if (tag == null || tag.length() == 0) {
            return
        }

        if (!hasTag(tag)) {
            File file = getTagFile(tag)
            return file.createNewFile()
        }

        return true
    }

    /**
     * 移除标记
     * @param tag
     */
    public boolean deleteTag(String tag) {
        if (tag == null || tag.length() == 0) {
            return
        }

        if (hasTag(tag)) {
            File file = getTagFile(tag)
            return file.delete()
        }
        return true
    }

    private File getTagFile(String tag) {
        return new File(getRootTagFile(),tag)
    }

    private File getRootTagFile() {
        File rootDir = new File(FastdexUtils.getBuildDir(project,variantName),"tags")
        FileUtils.ensumeDir(rootDir)
        return rootDir
    }
}
