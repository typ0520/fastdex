package fastdex.build.lib.snapshoot.file;

import fastdex.build.lib.snapshoot.api.DiffInfo;
import fastdex.build.lib.snapshoot.api.Status;

/**
 * 目录对比，file.length或者file.lastModified不一样时判定文件发生变化
 * Created by tong on 17/3/29.
 */
public class FileDiffInfo extends DiffInfo<FileNode> {
    public FileDiffInfo() {
    }

    public FileDiffInfo(Status status, FileNode now, FileNode old) {
        super(status, (now != null ? now.getUniqueKey() : old.getUniqueKey()), now, old);
    }
}
