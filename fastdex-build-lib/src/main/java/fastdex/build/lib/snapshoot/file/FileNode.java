package fastdex.build.lib.snapshoot.file;

import fastdex.build.lib.snapshoot.api.Node;
import fastdex.common.utils.DigestUtils;
import fastdex.common.utils.FileUtils;
import java.io.File;

/**
 * Created by tong on 17/3/29.
 */
public class FileNode extends Node {
    public String nodePath;
    public String md5;
    public long lastModified;
    public long fileLength;

    @Override
    public String getUniqueKey() {
        return nodePath;
    }

    @Override
    public boolean diffEquals(Node anNode) {
        if (!super.diffEquals(anNode))
            return false;

        FileNode fileNode = (FileNode) anNode;
        if (fileLength != fileNode.fileLength)
            return false;

        if (md5 != null) {
            return md5.equals(((FileNode) anNode).md5);
        }
        else {
            return lastModified == fileNode.lastModified;
        }
    }

    @Override
    public String toString() {
        return "FileNode{" +
                "nodePath='" + nodePath + '\'' +
                ", lastModified=" + lastModified +
                ", fileLength=" + fileLength +
                ", md5=" + md5 +
                '}';
    }

    public static FileNode create(File rootDir, File file, boolean useMd5,boolean useRelativePath) {
//相对路径作为key
        FileNode fileInfo = new FileNode();
        //fileInfo.absolutePath = file.getAbsolutePath();
        if (useRelativePath) {
            if (rootDir != null) {
                fileInfo.nodePath = rootDir.toPath().relativize(file.toPath()).toString();
            }
        }
        else {
            fileInfo.nodePath = file.getAbsolutePath();
        }

        fileInfo.lastModified = file.lastModified();
        fileInfo.fileLength = file.length();

        if (useMd5) {
            try {
                fileInfo.md5 = DigestUtils.md5DigestAsHex(FileUtils.readContents(file));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return fileInfo;
    }

    public static FileNode create(File rootDir, File file, boolean useMd5) {
        return create(rootDir,file,useMd5,true);
    }
}
