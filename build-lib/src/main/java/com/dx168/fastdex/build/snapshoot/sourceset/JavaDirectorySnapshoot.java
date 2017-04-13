package com.dx168.fastdex.build.snapshoot.sourceset;

import com.dx168.fastdex.build.snapshoot.file.BaseDirectorySnapshoot;
import com.dx168.fastdex.build.snapshoot.file.FileNode;
import com.dx168.fastdex.build.snapshoot.api.DiffInfo;
import com.dx168.fastdex.build.snapshoot.file.FileSuffixFilter;
import com.dx168.fastdex.build.snapshoot.file.ScanFilter;
import java.io.File;
import java.io.IOException;

/**
 * Created by tong on 17/3/30.
 */
public class JavaDirectorySnapshoot extends BaseDirectorySnapshoot<JavaFileDiffInfo,FileNode> {
    private static final FileSuffixFilter JAVA_SUFFIX_FILTER = new FileSuffixFilter(".java");

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

    public JavaDirectorySnapshoot(File directory, String... childPath) throws IOException {
        super(directory, childPath);
    }

    @Override
    protected JavaDirectoryDiffResultSet createEmptyResultSet() {
        return new JavaDirectoryDiffResultSet();
    }

    @Override
    protected DiffInfo createEmptyDiffInfo() {
        return new JavaFileDiffInfo();
    }
}
