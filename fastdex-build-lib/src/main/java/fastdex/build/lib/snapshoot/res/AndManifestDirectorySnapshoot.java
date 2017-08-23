package fastdex.build.lib.snapshoot.res;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import fastdex.build.lib.snapshoot.file.BaseDirectorySnapshoot;
import fastdex.build.lib.snapshoot.file.FileDiffInfo;
import fastdex.build.lib.snapshoot.file.FileNode;
import fastdex.build.lib.snapshoot.file.FileSuffixFilter;

/**
 * Created by tong on 17/8/22.
 */
public class AndManifestDirectorySnapshoot extends BaseDirectorySnapshoot<FileDiffInfo,FileNode> {
    private static final FileSuffixFilter SUFFIX_FILTER = new FileSuffixFilter(".xml");

    public AndManifestDirectorySnapshoot() throws IOException {
    }

    public AndManifestDirectorySnapshoot(AndManifestDirectorySnapshoot snapshoot) {
        super(snapshoot);
    }

    @Override
    public void addFile(File file) {
        addFile(file,SUFFIX_FILTER);
    }

    @Override
    protected FileNode createNode(String path, Path filePath) {
        File rootDir = null;
        if (path != null) {
            rootDir = new File(path);
        }
        return FileNode.create(rootDir,filePath.toFile(), useMd5,false);
    }
}
