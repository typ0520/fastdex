package com.dx168.fastdex.build.snapshoot.sourceset;

import com.dx168.fastdex.build.snapshoot.api.DiffResultSet;

/**
 * Created by tong on 17/3/29.
 */
public class JavaDirectoryDiffResultSet extends DiffResultSet<JavaFileDiffInfo> {

    public JavaDirectoryDiffResultSet() {
    }

    public JavaDirectoryDiffResultSet(JavaDirectoryDiffResultSet resultSet) {
        super(resultSet);
    }
}
