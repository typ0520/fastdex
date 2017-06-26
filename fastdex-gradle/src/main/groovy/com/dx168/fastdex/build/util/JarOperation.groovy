package com.dx168.fastdex.build.util

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import fastdex.build.lib.snapshoot.api.DiffResultSet
import com.dx168.fastdex.build.variant.FastdexVariant
import fastdex.common.utils.FileUtils
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
    public static void generatePatchJar(FastdexVariant fastdexVariant, TransformInvocation transformInvocation, File patchJar) throws IOException {
        Set<LibDependency> libraryDependencies = fastdexVariant.libraryDependencies
        Map<String,String> jarAndProjectPathMap = new HashMap<>()
        List<File> projectJarFiles = new ArrayList<>()
        //获取所有依赖工程的输出jar (compile project(':xxx'))
        for (LibDependency dependency : libraryDependencies) {
            projectJarFiles.add(dependency.jarFile)
            jarAndProjectPathMap.put(dependency.jarFile.absolutePath,dependency.dependencyProject.projectDir.absolutePath)
        }

        //所有的class目录
        Set<File> directoryInputFiles = new HashSet<>();
        //所有输入的jar
        Set<File> jarInputFiles = new HashSet<>();
        for (TransformInput input : transformInvocation.getInputs()) {
            Collection<DirectoryInput> directoryInputs = input.getDirectoryInputs()
            if (directoryInputs != null) {
                for (DirectoryInput directoryInput : directoryInputs) {
                    directoryInputFiles.add(directoryInput.getFile())
                }
            }

            if (!projectJarFiles.isEmpty()) {
                Collection<JarInput> jarInputs = input.getJarInputs()
                if (jarInputs != null) {
                    for (JarInput jarInput : jarInputs) {
                        if (projectJarFiles.contains(jarInput.getFile())) {
                            jarInputFiles.add(jarInput.getFile())
                        }
                    }
                }
            }
        }

        def project = fastdexVariant.project
        File tempDir = new File(fastdexVariant.buildDir,"temp")
        FileUtils.deleteDir(tempDir)
        FileUtils.ensumeDir(tempDir)

        Set<File> moudleDirectoryInputFiles = new HashSet<>()
        DiffResultSet diffResultSet = fastdexVariant.projectSnapshoot.diffResultSet
        for (File file : jarInputFiles) {
            String projectPath = jarAndProjectPathMap.get(file.absolutePath)
            List<String> patterns = diffResultSet.addOrModifiedClassesMap.get(projectPath)
            if (patterns != null && !patterns.isEmpty()) {
                File classesDir = new File(tempDir,"${file.name}-${System.currentTimeMillis()}")
                project.copy {
                    from project.zipTree(file)
                    for (String pattern : patterns) {
                        include pattern
                    }
                    into classesDir
                }
                moudleDirectoryInputFiles.add(classesDir)
                directoryInputFiles.add(classesDir)
            }
        }
        JarOperation.generatePatchJar(fastdexVariant,directoryInputFiles,moudleDirectoryInputFiles,patchJar);
    }

    /**
     * 生成补丁jar,仅把变化部分参与jar的生成
     * @param project
     * @param directoryInputFiles
     * @param outputJar
     * @param changedClassPatterns
     * @throws IOException
     */
    public static void generatePatchJar(FastdexVariant fastdexVariant, Set<File> directoryInputFiles,Set<File> moudleDirectoryInputFiles, File patchJar) throws IOException {
        long start = System.currentTimeMillis()
        def project = fastdexVariant.project
        project.logger.error("==fastdex generate patch jar start")

        if (directoryInputFiles == null || directoryInputFiles.isEmpty()) {
            throw new IllegalArgumentException("DirectoryInputFiles can not be null!!")
        }

        Set<String> changedClasses = fastdexVariant.projectSnapshoot.diffResultSet.addOrModifiedClasses
        if (fastdexVariant.configuration.hotClasses != null && fastdexVariant.configuration.hotClasses.length > 0) {
            String packageName = fastdexVariant.getOriginPackageName()
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

        ZipOutputStream outputJarStream = null
        try {
            outputJarStream = new ZipOutputStream(new FileOutputStream(patchJar));
            for (File classpathFile : directoryInputFiles) {
                Path classpath = classpathFile.toPath()

                boolean skip = (moudleDirectoryInputFiles != null && moudleDirectoryInputFiles.contains(classpathFile))

                Files.walkFileTree(classpath,new SimpleFileVisitor<Path>(){
                    @Override
                    FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!file.toFile().getName().endsWith(Constants.CLASS_SUFFIX)) {
                            return FileVisitResult.CONTINUE;
                        }
                        Path relativePath = classpath.relativize(file)
                        String entryName = relativePath.toString()
                        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                            entryName = entryName.replace("\\", "/");
                        }

                        if (skip) {
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
                        }
                        else {
                            String className = relativePath.toString().substring(0,relativePath.toString().length() - Constants.CLASS_SUFFIX.length());
                            className = className.replaceAll(Os.isFamily(Os.FAMILY_WINDOWS) ? "\\\\" : File.separator,"\\.")
                            for (String cn : changedClasses) {
                                if (cn.equals(className) || className.startsWith("${cn}\$")) {

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
