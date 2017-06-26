package fastdex.build.lib.snapshoot.file;

/**

 当前
 com/dx168/fastdex/sample/MainActivity.java
 老的
 com/dx168/fastdex/sample/MainActivity.java
 com/dx168/fastdex/sample/MainActivity2.java
 删除的是
 com/dx168/fastdex/sample/MainActivity2.java

 假如
 com/dx168/fastdex/sample/MainActivity.java
 com/dx168/fastdex/sample/MainActivity2.java
 老的
 com/dx168/fastdex/sample/MainActivity.java
 新增的是
 com/dx168/fastdex/sample/MainActivity2.java

 当前的
 com/dx168/fastdex/sample/MainActivity.java
 com/dx168/fastdex/sample/MainActivity2.java
 com/dx168/fastdex/sample/MainActivity3.java
 老的
 com/dx168/fastdex/sample/MainActivity.java
 com/dx168/fastdex/sample/MainActivity2.java
 com/dx168/fastdex/sample/MainActivity4.java
 新增的是
 com/dx168/fastdex/sample/MainActivity3.java
 删除的是
 com/dx168/fastdex/sample/MainActivity4.java

 除了删除的和新增的就是所有需要进行扫描的SourceSetInfo
 com/dx168/fastdex/sample/MainActivity.java
 com/dx168/fastdex/sample/MainActivity2.java
 */

import java.io.File;
import java.io.IOException;

/**
 * Created by tong on 17/3/29.
 */
public final class DirectorySnapshoot extends BaseDirectorySnapshoot<FileDiffInfo,FileNode> {
    public DirectorySnapshoot() {
    }

    public DirectorySnapshoot(BaseDirectorySnapshoot snapshoot) {
        super(snapshoot);
    }

    public DirectorySnapshoot(File directory) throws IOException {
        super(directory);
    }

    public DirectorySnapshoot(File directory, ScanFilter scanFilter) throws IOException {
        super(directory, scanFilter);
    }
}
