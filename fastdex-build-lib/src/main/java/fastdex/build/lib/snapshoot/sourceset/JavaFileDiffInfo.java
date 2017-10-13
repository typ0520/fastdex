package fastdex.build.lib.snapshoot.sourceset;

import fastdex.build.lib.snapshoot.api.Status;
import fastdex.build.lib.snapshoot.file.FileDiffInfo;
import fastdex.build.lib.snapshoot.file.FileNode;
import fastdex.common.ShareConstants;

/**
 * 目录对比，file.length或者file.lastModified不一样时判定文件发生变化
 * Created by tong on 17/3/29.
 */
public class JavaFileDiffInfo extends FileDiffInfo {
    public String path;

    public JavaFileDiffInfo() {
    }

    public JavaFileDiffInfo(Status status, FileNode now, FileNode old) {
        super(status, now, old);
    }

    public String getClassRelativePath() {
        String classRelativePath = null;
        if (uniqueKey.endsWith(ShareConstants.JAVA_SUFFIX)) {
            classRelativePath = uniqueKey.substring(0, uniqueKey.length() - ShareConstants.JAVA_SUFFIX.length());
        }
        else if (uniqueKey.endsWith(ShareConstants.KT_SUFFIX)) {
            classRelativePath = uniqueKey.substring(0, uniqueKey.length() - ShareConstants.KT_SUFFIX.length());
        }

        return classRelativePath;
    }
}
