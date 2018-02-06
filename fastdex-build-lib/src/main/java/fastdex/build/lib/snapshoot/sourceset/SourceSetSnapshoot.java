package fastdex.build.lib.snapshoot.sourceset;

import fastdex.build.lib.snapshoot.api.DiffInfo;
import fastdex.build.lib.snapshoot.api.DiffResultSet;
import fastdex.build.lib.snapshoot.api.Snapshoot;
import fastdex.build.lib.snapshoot.api.Status;
import fastdex.build.lib.snapshoot.file.FileNode;
import fastdex.build.lib.snapshoot.string.BaseStringSnapshoot;
import fastdex.build.lib.snapshoot.string.StringDiffInfo;
import fastdex.build.lib.snapshoot.string.StringNode;
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
        if (projectDir == null) {
            throw new RuntimeException("Invalid projectPath");
        }
        this.path = projectDir.getAbsolutePath();
        if (directorySnapshootSet == null) {
            directorySnapshootSet = new HashSet<>();
        }

        if (sourceSetFiles != null) {
            for (File sourceSet : sourceSetFiles) {
                if (sourceSet != null) {
                    JavaDirectorySnapshoot javaDirectorySnapshoot = new JavaDirectorySnapshoot(sourceSet);
                    javaDirectorySnapshoot.projectPath = projectDir.getAbsolutePath();
                    directorySnapshootSet.add(javaDirectorySnapshoot);
                }
            }
        }
    }

    public void addJavaDirectorySnapshoot(JavaDirectorySnapshoot javaDirectorySnapshoot) {
        nodes.add(StringNode.create(javaDirectorySnapshoot.path));
        directorySnapshootSet.add(javaDirectorySnapshoot);
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

            JavaDirectoryDiffResultSet resultSet = javaDirectorySnapshoot.createEmptyResultSet();
            for (FileNode node : javaDirectorySnapshoot.nodes) {
                resultSet.add(new JavaFileDiffInfo(Status.DELETEED,null,node));
            }
            sourceSetResultSet.mergeJavaDirectoryResultSet(path,resultSet);
        }

        for (DiffInfo diffInfo : sourceSetResultSet.getDiffInfos(Status.ADDED)) {
            JavaDirectorySnapshoot javaDirectorySnapshoot = getJavaDirectorySnapshootByPath(diffInfo.uniqueKey);

            JavaDirectoryDiffResultSet resultSet = javaDirectorySnapshoot.createEmptyResultSet();
            for (FileNode node : javaDirectorySnapshoot.nodes) {
                resultSet.add(new JavaFileDiffInfo(Status.ADDED,node,null));
            }
            sourceSetResultSet.mergeJavaDirectoryResultSet(path,resultSet);
        }

        for (DiffInfo diffInfo : sourceSetResultSet.getDiffInfos(Status.NOCHANGED)) {
            JavaDirectorySnapshoot now = getJavaDirectorySnapshootByPath(diffInfo.uniqueKey);
            JavaDirectorySnapshoot old = oldSnapshoot.getJavaDirectorySnapshootByPath(diffInfo.uniqueKey);

            JavaDirectoryDiffResultSet resultSet = (JavaDirectoryDiffResultSet) now.diff(old);
            sourceSetResultSet.mergeJavaDirectoryResultSet(now.path,resultSet);
        }

        return sourceSetResultSet;
    }

    public JavaDirectorySnapshoot getJavaDirectorySnapshootByPath(String path) {
        for (JavaDirectorySnapshoot snapshoot : directorySnapshootSet) {
            if (snapshoot.path.equals(path)) {
                return snapshoot;
            }
        }
        return null;
    }

    public JavaDirectorySnapshoot removeJavaDirectorySnapshootByPath(String path) {
        JavaDirectorySnapshoot result = null;
        for (JavaDirectorySnapshoot snapshoot : directorySnapshootSet) {
            if (snapshoot.path.equals(path)) {
                result = snapshoot;
            }
        }
        if (result != null) {
            directorySnapshootSet.remove(result);

            StringNode preDelNode = null;
            for (StringNode stringNode : nodes) {
                if (stringNode.getString().equals(path)) {
                    preDelNode = stringNode;
                }
            }

            if (preDelNode != null) {
                nodes.remove(preDelNode);
            }
        }
        return result;
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
