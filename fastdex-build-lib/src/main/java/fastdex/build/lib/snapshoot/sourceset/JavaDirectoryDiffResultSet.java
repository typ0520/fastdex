package fastdex.build.lib.snapshoot.sourceset;

import fastdex.build.lib.snapshoot.api.DiffResultSet;

/**
 * Created by tong on 17/3/29.
 */
public class JavaDirectoryDiffResultSet extends DiffResultSet<JavaFileDiffInfo> {
    public String projectPath;

    public JavaDirectoryDiffResultSet() {
    }

    public JavaDirectoryDiffResultSet(JavaDirectoryDiffResultSet resultSet) {
        super(resultSet);
    }
}
