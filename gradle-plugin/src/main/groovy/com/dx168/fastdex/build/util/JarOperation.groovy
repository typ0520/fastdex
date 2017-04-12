package com.dx168.fastdex.build.util

import com.dx168.fastdex.build.variant.FastdexVariant
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleException
import org.objectweb.asm.Opcodes
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * jar操作
 * Created by tong on 17/11/4.
 */
public class JarOperation implements Opcodes {
    /**
     * 生成补丁jar,仅把变化部分参与jar的生成
     * @param project
     * @param directoryInputFiles
     * @param outputJar
     * @param changedClassPatterns
     * @throws IOException
     */
    public static void generatePatchJar(FastdexVariant fastdexVariant, Set<File> directoryInputFiles, File patchJar) throws IOException {
        long start = System.currentTimeMillis()
        def project = fastdexVariant.project
        project.logger.error("==fastdex generate patch jar start")

        if (directoryInputFiles == null || directoryInputFiles.isEmpty()) {
            throw new IllegalArgumentException("DirectoryInputFiles can not be null!!")
        }

        Set<String> changedClasses = fastdexVariant.projectSnapshoot.diffResultSet.addOrModifiedClasses
        if (fastdexVariant.configuration.hotClasses != null) {
            String packageName = fastdexVariant.getApplicationPackageName()
            for (String str : fastdexVariant.configuration.hotClasses) {
                if (str != null) {
                    changedClasses.add(str.replaceAll("\\{package\\}",packageName))
                }
            }
        }

        if (project.fastdex.debug) {
            project.logger.error("==fastdex debug changedClasses: ${changedClasses}")
        }

        if (changedClasses == null || changedClasses.isEmpty()) {
            throw new IllegalArgumentException("No java files changed!!")
        }

        FileUtils.deleteFile(patchJar)

        boolean willExeDexMerge = fastdexVariant.willExecDexMerge()

        ZipOutputStream outputJarStream = new ZipOutputStream(new FileOutputStream(patchJar));
        try {
            for (File classpathFile : directoryInputFiles) {
                Path classpath = classpathFile.toPath()
                Files.walkFileTree(classpath,new SimpleFileVisitor<Path>(){
                    @Override
                    FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!file.toFile().getName().endsWith(Constant.CLASS_SUFFIX)) {
                            return FileVisitResult.CONTINUE;
                        }
                        Path relativePath = classpath.relativize(file)
                        String className = relativePath.toString().substring(0,relativePath.toString().length() - Constant.CLASS_SUFFIX.length());
                        className = className.replaceAll(Os.isFamily(Os.FAMILY_WINDOWS) ? "\\\\" : File.separator,"\\.")

                        for (String cn : changedClasses) {
                            if (cn.equals(className) || className.startsWith("${cn}\$")) {
                                String entryName = relativePath.toString()
                                if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                                    entryName = entryName.replace("\\", "/");
                                }

                                ZipEntry e = new ZipEntry(entryName)
                                outputJarStream.putNextEntry(e)

                                if (project.fastdex.debug) {
                                    project.logger.error("==fastdex add entry: ${e}")
                                }
                                byte[] bytes = FileUtils.readContents(file.toFile())
                                //如果需要触发dex merge,必须注入代码
                                if (willExeDexMerge) {
                                    bytes = ClassInject.inject(bytes)
                                    project.logger.error("==fastdex inject: ${entryName}")
                                }
                                outputJarStream.write(bytes,0,bytes.length)
                                outputJarStream.closeEntry()
                                break;
                            }
                        }
                        return FileVisitResult.CONTINUE
                    }
                })
            }

        } finally {
            if (outputJarStream != null) {
                outputJarStream.close();
            }
        }

        if (!FileUtils.isLegalFile(patchJar)) {
            throw new GradleException("==fastdex generate patch jar fail: ${patchJar}")
        }
        long end = System.currentTimeMillis();
        project.logger.error("==fastdex generate patch jar complete: ${patchJar} use: ${end - start}ms")
    }
}
