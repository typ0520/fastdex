package com.dx168.fastdex.build.snapshoot.file;

import com.dx168.fastdex.build.snapshoot.api.Node;

import java.io.File;

/**
 * Created by tong on 17/3/29.
 */
public class FileNode extends Node {
    //public String absolutePath;
    public String relativePath;
    public long lastModified;
    public long fileLength;

    @Override
    public String getUniqueKey() {
        return relativePath;
    }

    @Override
    public boolean diffEquals(Node anNode) {
        if (this == anNode) return true;
        if (anNode == null) return false;

        FileNode fileNode = (FileNode) anNode;
        if (lastModified != fileNode.lastModified) return false;
        if (fileLength != fileNode.fileLength) return false;
        return equals(fileNode);
    }

    @Override
    public String toString() {
        return "FileNode{" +
                "relativePath='" + relativePath + '\'' +
                ", lastModified=" + lastModified +
                ", fileLength=" + fileLength +
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
}
