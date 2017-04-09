package com.dx168.fastdex.build.snapshoot.sourceset;

import com.dx168.fastdex.build.snapshoot.api.DiffInfo;
import com.dx168.fastdex.build.snapshoot.api.DiffResultSet;
import com.dx168.fastdex.build.snapshoot.api.Snapshoot;
import com.dx168.fastdex.build.snapshoot.api.Status;
import com.dx168.fastdex.build.snapshoot.file.FileNode;
import com.dx168.fastdex.build.snapshoot.string.BaseStringSnapshoot;
import com.dx168.fastdex.build.snapshoot.string.StringDiffInfo;
import com.dx168.fastdex.build.snapshoot.string.StringNode;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tong on 17/3/31.
 */
public final class SourceSetSnapshoot extends BaseStringSnapshoot<StringDiffInfo,StringNode> {
    public String path;//工程目录

    @SerializedName("sourceSets")
    public Set<JavaDirectorySnapshoot> directorySnapshootSet = new HashSet<>();

    public SourceSetSnapshoot() {
    }

    public SourceSetSnapshoot(SourceSetSnapshoot snapshoot) {
        super(snapshoot);
        //from gson
        this.path = snapshoot.path;
        this.directorySnapshootSet.addAll(snapshoot.directorySnapshootSet);
    }

    public SourceSetSnapshoot(File projectDir, Set<File> sourceSets) throws IOException {
        super(SourceSetSnapshoot.getSourceSetStringArray(sourceSets));
        init(projectDir,sourceSets);
    }

    public SourceSetSnapshoot(File projectDir, String ...sourceSets) throws IOException {
        super(sourceSets);

        Set<File> result = new HashSet<>();
        if (sourceSets != null) {
            for (String string : sourceSets) {
                result.add(new File(string));
            }
        }
        init(projectDir,result);
    }

    private void init(File projectDir,Set<File> sourceSetFiles) throws IOException {
        if (projectDir == null || projectDir.length() == 0) {
            throw new RuntimeException("Invalid projectPath");
        }
        this.path = projectDir.getAbsolutePath();
        if (directorySnapshootSet == null) {
            directorySnapshootSet = new HashSet<>();
        }

        for (File sourceSet : sourceSetFiles) {
            directorySnapshootSet.add(new JavaDirectorySnapshoot(sourceSet));
        }
    }

    @Override
    protected SourceSetDiffResultSet createEmptyResultSet() {
        return new SourceSetDiffResultSet();
    }

    @Override
    public DiffResultSet<StringDiffInfo> diff(Snapshoot<StringDiffInfo, StringNode> otherSnapshoot) {
        SourceSetDiffResultSet sourceSetResultSet = (SourceSetDiffResultSet) super.diff(otherSnapshoot);

        SourceSetSnapshoot oldSnapshoot = (SourceSetSnapshoot)otherSnapshoot;
        for (DiffInfo diffInfo : sourceSetResultSet.getDiffInfos(Status.DELETEED)) {
            JavaDirectorySnapshoot javaDirectorySnapshoot = oldSnapshoot.getJavaDirectorySnapshootByPath(diffInfo.uniqueKey);
            for (FileNode node : javaDirectorySnapshoot.nodes) {
                sourceSetResultSet.addJavaFileDiffInfo(new JavaFileDiffInfo(Status.DELETEED,null,node));
            }
        }

        for (DiffInfo diffInfo : sourceSetResultSet.getDiffInfos(Status.ADDED)) {
            JavaDirectorySnapshoot javaDirectorySnapshoot = getJavaDirectorySnapshootByPath(diffInfo.uniqueKey);
            for (FileNode node : javaDirectorySnapshoot.nodes) {
                sourceSetResultSet.addJavaFileDiffInfo(new JavaFileDiffInfo(Status.ADDED,node,null));
            }
        }

        for (DiffInfo diffInfo : sourceSetResultSet.getDiffInfos(Status.NOCHANGED)) {
            JavaDirectorySnapshoot now = getJavaDirectorySnapshootByPath(diffInfo.uniqueKey);
            JavaDirectorySnapshoot old = oldSnapshoot.getJavaDirectorySnapshootByPath(diffInfo.uniqueKey);

            JavaDirectoryDiffResultSet resultSet = (JavaDirectoryDiffResultSet) now.diff(old);
            sourceSetResultSet.mergeJavaDirectoryResultSet(now.path,resultSet);
        }

        return sourceSetResultSet;
    }

    private JavaDirectorySnapshoot getJavaDirectorySnapshootByPath(String path) {
        for (JavaDirectorySnapshoot snapshoot : directorySnapshootSet) {
            if (snapshoot.path.equals(path)) {
                return snapshoot;
            }
        }
        return null;
    }

    /**
     * 工程路径是否发生变化
     * @param currentProjectDir
     * @return
     */
    public boolean isProjectDirChanged(File currentProjectDir) {
        return !currentProjectDir.getAbsolutePath().equals(path);
    }

    /**
     * 检查工程路径是否能对应上，如果对应不上使用参数指定的路径
     * @param currentProjectDir
     * @return 如果发生变化返回true，反之返回false
     */
    public boolean ensumeProjectDir(File currentProjectDir) {
        if (!isProjectDirChanged(currentProjectDir)) {
            return false;
        }

        applyNewProjectDir(currentProjectDir);
        return true;
    }

    private void applyNewProjectDir(File currentProjectDir) {
        String oldProjectDir = path;
        this.path = currentProjectDir.getAbsolutePath();

        for (StringNode node : nodes) {
            node.setString(node.getString().replaceAll(oldProjectDir,this.path));
        }
        for (JavaDirectorySnapshoot snapshoot : directorySnapshootSet) {
            snapshoot.path = snapshoot.path.replaceAll(oldProjectDir,this.path);
        }
    }

    @Override
    public String toString() {
        return "SourceSetSnapshoot{" +
                "path='" + path + '\'' +
                ", directorySnapshootSet=" + directorySnapshootSet +
                '}';
    }

    public static Set<String> getSourceSetStringArray(Set<File> sourceSets) {
        Set<String> result = new HashSet<>();
        if (sourceSets != null) {
            for (File file : sourceSets) {
                result.add(file.getAbsolutePath());
            }
        }
        return result;
    }
}
