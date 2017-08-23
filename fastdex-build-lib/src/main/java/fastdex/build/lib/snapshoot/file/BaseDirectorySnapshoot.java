package fastdex.build.lib.snapshoot.file;

import fastdex.build.lib.snapshoot.api.DiffInfo;
import fastdex.build.lib.snapshoot.api.DiffResultSet;
import fastdex.build.lib.snapshoot.api.Snapshoot;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 目录快照
 * Created by tong on 17/3/29.
 */
public class BaseDirectorySnapshoot<DIFF_INFO extends FileDiffInfo,NODE extends FileNode> extends Snapshoot<DIFF_INFO,NODE> {
    public String path;
    public boolean useMd5;

    public BaseDirectorySnapshoot() {
    }

    public BaseDirectorySnapshoot(BaseDirectorySnapshoot snapshoot) {
        super(snapshoot);
        this.path = snapshoot.path;
    }

    public BaseDirectorySnapshoot(File directory) throws IOException {
        this(directory,(ScanFilter)null);
    }

    public BaseDirectorySnapshoot(File directory, ScanFilter scanFilter) throws IOException {
        this(directory, null, scanFilter,false);
    }

    public BaseDirectorySnapshoot(File directory, String ...childPaths) throws IOException {
        this(directory, toFileList(childPaths));
    }

    public BaseDirectorySnapshoot(File directory, Collection<File> childPath) throws IOException {
        this(directory,childPath,null, false);
    }

    public BaseDirectorySnapshoot(File directory,boolean useMd5, String ...childPaths) throws IOException {
        this(directory,toFileList(childPaths),null, useMd5);
    }

    public BaseDirectorySnapshoot(File directory, Collection<File> childPaths, ScanFilter scanFilter,boolean useMd5) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("Directory can not be null!!");
        }

        this.path = directory.getAbsolutePath();
        this.useMd5 = useMd5;

        if (childPaths == null) {
            if (directory.exists() && directory.isDirectory()) {
                walkFileTree(directory,scanFilter);
            }
        }
        else {
            for (File f : childPaths) {
                if (f != null) {
                    visitFile(f.toPath(),null,scanFilter);
                }
            }
        }
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

    public void addFile(File file) {
        addFile(file,null);
    }

    public void addFile(File file,ScanFilter scanFilter) {
        try {
            visitFile(file.toPath(),null,scanFilter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs,ScanFilter scanFilter) throws IOException {
        if (scanFilter != null) {
            if (!scanFilter.preVisitFile(filePath.toFile())) {
                return FileVisitResult.CONTINUE;
            }
        }
        addNode(createNode(path, filePath));
        return FileVisitResult.CONTINUE;
    }

    protected NODE createNode(String path, Path filePath) {
        File rootDir = null;
        if (path != null) {
            rootDir = new File(path);
        }
        return (NODE) FileNode.create(rootDir,filePath.toFile(), useMd5);
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

    private static Collection<File> toFileList(String[] childPaths) {
        List<File> files = new ArrayList<>();
        if (childPaths != null) {
            for (String path : childPaths) {
                if (path != null) {
                    files.add(new File(path));
                }
            }
        }
        return files;
    }
}
