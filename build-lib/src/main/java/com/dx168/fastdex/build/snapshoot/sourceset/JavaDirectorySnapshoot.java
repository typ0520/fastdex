package com.dx168.fastdex.build.snapshoot.sourceset;

import com.dx168.fastdex.build.snapshoot.file.BaseDirectorySnapshoot;
import com.dx168.fastdex.build.snapshoot.file.FileNode;
import com.dx168.fastdex.build.snapshoot.api.DiffInfo;
import com.dx168.fastdex.build.snapshoot.file.FileSuffixFilter;
import java.io.File;
import java.io.IOException;

/**
 * Created by tong on 17/3/30.
 */
public final class JavaDirectorySnapshoot extends BaseDirectorySnapshoot<JavaFileDiffInfo,FileNode> {
    private static final FileSuffixFilter JAVA_SUFFIX_FILTER = new FileSuffixFilter(".java");

    public JavaDirectorySnapshoot() {
    }

    public JavaDirectorySnapshoot(JavaDirectorySnapshoot snapshoot) {
        super(snapshoot);
    }

    public JavaDirectorySnapshoot(File directory) throws IOException {
        super(directory, JAVA_SUFFIX_FILTER);
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
