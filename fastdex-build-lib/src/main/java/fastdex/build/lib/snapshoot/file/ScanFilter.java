package fastdex.build.lib.snapshoot.file;

import java.io.File;

/**
 * Created by tong on 17/3/29.
 */
public interface ScanFilter {
    /**
     * 如果返回true处理这个文件，反之忽略
     * @param file
     * @return
     */
    boolean preVisitFile(File file);
}
