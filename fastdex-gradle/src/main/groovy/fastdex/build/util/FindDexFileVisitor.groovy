package fastdex.build.util;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import fastdex.common.ShareConstants;

/**
 * Created by tong on 17/9/24.
 */
public class FindDexFileVisitor extends SimpleFileVisitor<Path> {
    public boolean hasDex;

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.toString().endsWith(ShareConstants.DEX_SUFFIX)) {
            hasDex = true;
            return FileVisitResult.TERMINATE;
        }
        return FileVisitResult.CONTINUE;
    }
}
