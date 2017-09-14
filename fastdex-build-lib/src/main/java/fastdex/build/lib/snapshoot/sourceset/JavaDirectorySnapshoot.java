package fastdex.build.lib.snapshoot.sourceset;

import fastdex.build.lib.snapshoot.file.BaseDirectorySnapshoot;
import fastdex.build.lib.snapshoot.file.FileNode;
import fastdex.build.lib.snapshoot.api.DiffInfo;
import fastdex.build.lib.snapshoot.file.FileSuffixFilter;
import com.google.gson.annotations.Expose;
import java.io.File;
import java.io.IOException;

/**
 * Created by tong on 17/3/30.
 */
public class JavaDirectorySnapshoot extends BaseDirectorySnapshoot<JavaFileDiffInfo,FileNode> {
    private static final FileSuffixFilter JAVA_SUFFIX_FILTER = new FileSuffixFilter(".java",".kt");
    @Expose
    public String projectPath;

    public JavaDirectorySnapshoot() {
    }

    public JavaDirectorySnapshoot(JavaDirectorySnapshoot snapshoot) {
        super(snapshoot);
    }

    public JavaDirectorySnapshoot(File directory) throws IOException {
        super(directory, JAVA_SUFFIX_FILTER);
    }

    public JavaDirectorySnapshoot(File directory, boolean useMd5) throws IOException {
        super(directory,JAVA_SUFFIX_FILTER, useMd5);
    }

    public JavaDirectorySnapshoot(File directory,FileSuffixFilter filter, boolean useMd5) throws IOException {
        super(directory,filter, useMd5);
    }

    public JavaDirectorySnapshoot(File directory,boolean useMd5, String ...childPath) throws IOException {
        super(directory, toFileList(childPath),JAVA_SUFFIX_FILTER,useMd5);
    }

    @Override
    protected JavaDirectoryDiffResultSet createEmptyResultSet() {
        JavaDirectoryDiffResultSet javaDirectoryDiffResultSet = new JavaDirectoryDiffResultSet();
        javaDirectoryDiffResultSet.projectPath = projectPath;
        return javaDirectoryDiffResultSet;
    }

    @Override
    protected DiffInfo createEmptyDiffInfo() {
        return new JavaFileDiffInfo();
    }
}
