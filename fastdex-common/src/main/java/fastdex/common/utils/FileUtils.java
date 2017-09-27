package fastdex.common.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import fastdex.common.ShareConstants;

/**
 * Created by tong on 17/3/10.
 */
public class FileUtils {
    public static final int BUFFER_SIZE = 16384;

    public static final boolean ensumeDir(File file) {
        if (file == null) {
            return false;
        }
        if (!fileExists(file.getAbsolutePath())) {
            return file.mkdirs();
        }
        return true;
    }

    public static final boolean fileExists(String filePath) {
        if (filePath == null) {
            return false;
        }

        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return true;
        }
        return false;
    }

    public static final boolean dirExists(String filePath) {
        if (filePath == null) {
            return false;
        }

        File file = new File(filePath);
        if (file.exists() && file.isDirectory()) {
            return true;
        }
        return false;
    }

    public static final boolean deleteFile(String filePath) {
        if (filePath == null) {
            return true;
        }

        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    public static final boolean deleteFile(File file) {
        if (file == null) {
            return true;
        }
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    public static boolean isLegalFile(File file) {
        if (file == null) {
            return false;
        }
        return file.exists() && file.isFile() && file.length() > 0;
    }

    public static long getFileSizes(File f) {
        if (f == null) {
            return 0;
        }
        long size = 0;
        if (f.exists() && f.isFile()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
                size = fis.available();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return size;
    }

    public static final boolean deleteDir(File file) {
        if (file == null || (!file.exists())) {
            return false;
        }
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteDir(files[i]);
            }
        }
        file.delete();
        return true;
    }

    public static void cleanDir(File dir) {
        if (dir.exists()) {
            FileUtils.deleteDir(dir);
            dir.mkdirs();
        }
    }

    public static void copyResourceUsingStream(String name, File dest) throws IOException {
        FileOutputStream os = null;
        File parent = dest.getParentFile();
        if (parent != null && (!parent.exists())) {
            parent.mkdirs();
        }
        InputStream is = null;

        try {
            is = FileUtils.class.getResourceAsStream("/" + name);
            os = new FileOutputStream(dest, false);

            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    public static void copyFileUsingStream(File source, File dest) throws IOException {
        FileInputStream is = null;
        FileOutputStream os = null;
        File parent = dest.getParentFile();
        if (parent != null && (!parent.exists())) {
            parent.mkdirs();
        }
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest, false);

            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            dest.setLastModified(source.lastModified());
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    public static void write2file(byte[] content, File dest) throws IOException {
        FileOutputStream os = null;
        File parent = dest.getParentFile();
        if (parent != null && (!parent.exists())) {
            parent.mkdirs();
        }
        try {
            os = new FileOutputStream(dest, false);
            os.write(content);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    public static byte[] readContents(final File file) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final int bufferSize = BUFFER_SIZE;
        try {
            final FileInputStream fis = new FileInputStream(file);
            final BufferedInputStream bIn = new BufferedInputStream(fis);
            int length;
            byte[] buffer = new byte[bufferSize];
            byte[] bufferCopy;
            while ((length = bIn.read(buffer, 0, bufferSize)) != -1) {
                bufferCopy = new byte[length];
                System.arraycopy(buffer, 0, bufferCopy, 0, length);
                output.write(bufferCopy);
            }
            bIn.close();
        } finally {
            output.close();
        }
        return output.toByteArray();
    }

    public static byte[] readStream(final InputStream is) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final int bufferSize = BUFFER_SIZE;
        try {
            final BufferedInputStream bIn = new BufferedInputStream(is);
            int length;
            byte[] buffer = new byte[bufferSize];
            byte[] bufferCopy;
            while ((length = bIn.read(buffer, 0, bufferSize)) != -1) {
                bufferCopy = new byte[length];
                System.arraycopy(buffer, 0, bufferCopy, 0, length);
                output.write(bufferCopy);
            }
            bIn.close();
        } finally {
            output.close();
            is.close();
        }
        return output.toByteArray();
    }

    public static int copyDir(File sourceDir, File destDir, final String suffix) throws IOException {
        return copyDir(sourceDir, destDir, suffix, true);
    }

    public static int copyDir(File sourceDir, File destDir, final String suffix, final boolean override) throws IOException {
        final Path sourcePath = sourceDir.toPath();
        final Path destPath = destDir.toPath();


        class MySimpleFileVisitor extends SimpleFileVisitor<Path> {
            public int totalSize = 0;

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (suffix != null && !file.toFile().getName().endsWith(suffix)) {
                    return FileVisitResult.CONTINUE;
                }
                Path relativePath = sourcePath.relativize(file);
                Path classFilePath = destPath.resolve(relativePath);

                File source = file.toFile();
                File dest = classFilePath.toFile();

                if (override || !isLegalFile(dest)) {
                    //System.out.println("dest: " + dest);
                    copyFileUsingStream(source,dest);
                    dest.setLastModified(source.lastModified());
                    totalSize++;
                }
                return FileVisitResult.CONTINUE;
            }
        }

        MySimpleFileVisitor simpleFileVisitor = new MySimpleFileVisitor();
        Files.walkFileTree(sourceDir.toPath(),simpleFileVisitor);
        return simpleFileVisitor.totalSize;
    }

    public static int moveDir(File sourceDir, File destDir, final String suffix) throws IOException {
        final Path sourcePath = sourceDir.toPath();
        final Path destPath = destDir.toPath();


        class MoveDirSimpleFileVisitor extends SimpleFileVisitor<Path> {
            public int totalSize = 0;

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (suffix != null && !file.toFile().getName().endsWith(suffix)) {
                    return FileVisitResult.CONTINUE;
                }
                Path relativePath = sourcePath.relativize(file);
                Path classFilePath = destPath.resolve(relativePath);

                File source = file.toFile();
                File dest = classFilePath.toFile();

                //System.out.println("dest: " + dest);
                source.renameTo(dest);
                dest.setLastModified(source.lastModified());
                totalSize++;
                return FileVisitResult.CONTINUE;
            }
        }

        MoveDirSimpleFileVisitor simpleFileVisitor = new MoveDirSimpleFileVisitor();
        Files.walkFileTree(sourceDir.toPath(),simpleFileVisitor);
        return simpleFileVisitor.totalSize;
    }

    public static int copyDir(File sourceDir, File destDir) throws IOException {
        return copyDir(sourceDir,destDir,null);
    }

//    public static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation) throws IOException {
//        if (sourceLocation.isDirectory()) {
//            if (!targetLocation.exists()) {
//                targetLocation.mkdir();
//            }
//            String[] children = sourceLocation.list();
//            for (int i = 0; i < children.length; i++) {
//                copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
//            }
//        } else {
//            copyFileUsingStream(sourceLocation,targetLocation);
//        }
//    }


    /**
     * 目录中是否存在dex
     * @param dir
     * @return
     */
    public static boolean hasDex(File dir) {
        if (!dirExists(dir.getAbsolutePath())) {
            return false;
        }

        //check dex
        boolean result = false;
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(ShareConstants.DEX_SUFFIX)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
