package com.dx168.fastdex.build.snapshoot.file;

import com.dx168.fastdex.build.snapshoot.api.DiffInfo;
import com.dx168.fastdex.build.snapshoot.api.DiffResultSet;
import com.dx168.fastdex.build.snapshoot.api.Snapshoot;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * 目录快照
 * Created by tong on 17/3/29.
 */
public class BaseDirectorySnapshoot<DIFF_INFO extends FileDiffInfo,NODE extends FileNode> extends Snapshoot<DIFF_INFO,NODE> {
    public String path;

    public BaseDirectorySnapshoot() {
    }

    public BaseDirectorySnapshoot(BaseDirectorySnapshoot snapshoot) {
        super(snapshoot);
        this.path = snapshoot.path;
    }

    public BaseDirectorySnapshoot(File directory) throws IOException {
        this(directory,null);
    }

    public BaseDirectorySnapshoot(File directory, ScanFilter scanFilter) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("Directory can not be null!!");
        }
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory: " + directory);
        }
        this.path = directory.getAbsolutePath();
        walkFileTree(directory,scanFilter);
    }

    @Override
    protected DiffInfo createEmptyDiffInfo() {
        return new FileDiffInfo();
    }

    protected void walkFileTree(File directory, final ScanFilter scanFilter) throws IOException {
        Files.walkFileTree(directory.toPath(),new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                return BaseDirectorySnapshoot.this.visitFile(file,attrs,scanFilter);
            }
        });
    }

    protected FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs,ScanFilter scanFilter) throws IOException {
        if (scanFilter != null) {
            if (!scanFilter.preVisitFile(filePath.toFile())) {
                return FileVisitResult.CONTINUE;
            }
        }
        addNode((NODE) FileNode.create(new File(path),filePath.toFile()));
        return FileVisitResult.CONTINUE;
    }

    public File getAbsoluteFile(FileNode fileItemInfo) {
        return new File(path,fileItemInfo.getUniqueKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseDirectorySnapshoot<?, ?> that = (BaseDirectorySnapshoot<?, ?>) o;

        return path != null ? path.equals(that.path) : that.path == null;
    }



    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }

    public static DiffResultSet<FileDiffInfo> diff(File now, File old) throws IOException {
        return BaseDirectorySnapshoot.diff(now,old,null);
    }

    public static DiffResultSet<FileDiffInfo> diff(File now, File old, ScanFilter scanFilter) throws IOException {
        return  new BaseDirectorySnapshoot(now,scanFilter).diff(new BaseDirectorySnapshoot(old,scanFilter));
    }

    @Override
    public String toString() {
        return "BaseDirectorySnapshoot{" +
                "path='" + path + '\'' +
                '}';
    }
}
