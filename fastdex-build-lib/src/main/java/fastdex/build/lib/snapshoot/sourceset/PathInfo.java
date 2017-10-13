package fastdex.build.lib.snapshoot.sourceset;

import java.io.File;

/**
 * Created by tong on 17/4/6.
 */
public class PathInfo {
    public String path;
    public File absoluteFile;
    public String relativePath;

    public PathInfo(File absoluteFile, String relativePath) {
        this.absoluteFile = absoluteFile;
        this.relativePath = relativePath;
    }

    public PathInfo(String path, File absoluteFile, String relativePath) {
        this.path = path;
        this.absoluteFile = absoluteFile;
        this.relativePath = relativePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PathInfo pathInfo = (PathInfo) o;

        if (absoluteFile != null ? !absoluteFile.equals(pathInfo.absoluteFile) : pathInfo.absoluteFile != null)
            return false;
        return relativePath != null ? relativePath.equals(pathInfo.relativePath) : pathInfo.relativePath == null;

    }

    @Override
    public int hashCode() {
        int result = absoluteFile != null ? absoluteFile.hashCode() : 0;
        result = 31 * result + (relativePath != null ? relativePath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PathInfo{" +
                "absoluteFile=" + absoluteFile +
                ", nodePath='" + relativePath + '\'' +
                '}';
    }
}
