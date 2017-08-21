package fastdex.build.lib.snapshoot.file;

import fastdex.build.lib.snapshoot.api.Node;
import fastdex.common.utils.DigestUtils;
import fastdex.common.utils.FileUtils;

import java.io.File;

/**
 * Created by tong on 17/3/29.
 */
public class FileNode extends Node {
    //public String absolutePath;
    public String relativePath;
    public long lastModified;
    public long fileLength;
    public String md5;

    @Override
    public String getUniqueKey() {
        return relativePath;
    }

    @Override
    public boolean diffEquals(Node anNode) {
        if (this == anNode) return true;
        if (anNode == null) return false;

        FileNode fileNode = (FileNode) anNode;
        if (fileLength != fileNode.fileLength) return false;


        if (lastModified != fileNode.lastModified) {
            if (md5 != null && md5.equals(((FileNode) anNode).md5)) {
                return true;
            }
            return false;
        }
        return equals(fileNode);
    }

    @Override
    public String toString() {
        return "FileNode{" +
                "relativePath='" + relativePath + '\'' +
                ", lastModified=" + lastModified +
                ", fileLength=" + fileLength +
                ", md5=" + md5 +
                '}';
    }

    public static FileNode create(File rootDir, File file) {
        //相对路径作为key
        FileNode fileInfo = new FileNode();
        //fileInfo.absolutePath = file.getAbsolutePath();
        fileInfo.relativePath = rootDir.toPath().relativize(file.toPath()).toString();

        fileInfo.lastModified = file.lastModified();
        fileInfo.fileLength = file.length();
        return fileInfo;
    }

    public static FileNode create(File rootDir, File file, boolean useMd5) {
        //相对路径作为key
        FileNode fileInfo = new FileNode();
        //fileInfo.absolutePath = file.getAbsolutePath();
        fileInfo.relativePath = rootDir.toPath().relativize(file.toPath()).toString();

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
}
