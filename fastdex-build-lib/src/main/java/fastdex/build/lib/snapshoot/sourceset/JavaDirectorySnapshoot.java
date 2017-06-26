package fastdex.build.lib.snapshoot.sourceset;

import fastdex.build.lib.snapshoot.file.BaseDirectorySnapshoot;
import fastdex.build.lib.snapshoot.file.FileNode;
import fastdex.build.lib.snapshoot.api.DiffInfo;
import fastdex.build.lib.snapshoot.file.FileSuffixFilter;
import fastdex.build.lib.snapshoot.file.ScanFilter;
import com.google.gson.annotations.Expose;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by tong on 17/3/30.
 */
public class JavaDirectorySnapshoot extends BaseDirectorySnapshoot<JavaFileDiffInfo,FileNode> {
    private static final FileSuffixFilter JAVA_SUFFIX_FILTER = new FileSuffixFilter(".java");
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

    public JavaDirectorySnapshoot(File directory, ScanFilter scanFilter) throws IOException {
        super(directory, scanFilter);
    }

    public JavaDirectorySnapshoot(File directory, String ...childPath) throws IOException {
        super(directory, childPath);
    }

    public JavaDirectorySnapshoot(File directory, Collection<File> childPath) throws IOException {
        super(directory, childPath);
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
