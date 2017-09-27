package fastdex.build.util

import fastdex.common.ShareConstants
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * Created by tong on 17/9/24.
 */
public class HookNormalBuildDexoutoutDirFileVisitor extends SimpleFileVisitor<Path> {
    public File dexOutputDir
    private int point

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.toString().endsWith(ShareConstants.DEX_SUFFIX)) {
            File dir = file.parent.toFile()

            if (dexOutputDir == null) {
                dexOutputDir == dir
            }

            if (!dir.equals(dexOutputDir)) {

            }
        }
        return FileVisitResult.CONTINUE;
    }
}
