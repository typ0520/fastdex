package fastdex.build.util

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import fastdex.build.variant.FastdexVariant
import fastdex.common.utils.FileUtils
import org.objectweb.asm.*
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 source class:
 ''''''
 public class MainActivity {

 }
 '''''

 dest class:
 ''''''
 import fastdex.runtime.antilazyload.AntilazyLoad;

 public class MainActivity {
 public MainActivity() {
 System.out.println(Antilazyload.str);
 }
 }
 ''''''
 * 代码注入，往所有的构造方法中添加对fastdex.runtime.antilazyload.AntilazyLoad的依赖
 * Created by tong on 17/10/3.
 */
class ClassInject implements Opcodes {
    private static final boolean DEBUG_INJECT = false

    /**
     * 注入class目录和jar文件
     * @param fastdexVariant
     * @param transformInvocation
     */
     static void injectTransformInvocation(FastdexVariant fastdexVariant, TransformInvocation transformInvocation) {
        //所有的class目录
        HashSet<File> directoryInputFiles = new HashSet<>()
        //所有输入的jar
        HashSet<File> jarInputFiles = new HashSet<>()
        for (TransformInput input : transformInvocation.getInputs()) {
            Collection<DirectoryInput> directoryInputs = input.getDirectoryInputs()
            if (directoryInputs != null) {
                for (DirectoryInput directoryInput : directoryInputs) {
                    directoryInputFiles.add(directoryInput.getFile())
                }
            }

            Collection<JarInput> jarInputs = input.getJarInputs()
            if (jarInputs != null) {
                for (JarInput jarInput : jarInputs) {
                    jarInputFiles.add(jarInput.getFile())
                }
            }
        }
        injectDirectoryInputFiles(fastdexVariant,directoryInputFiles)
        injectJarInputFiles(fastdexVariant,jarInputFiles)
    }

    /**
     * 往所有项目代码里注入解决pre-verify问题的code
     * @param directoryInputFiles
     */
    static void injectDirectoryInputFiles(FastdexVariant fastdexVariant, HashSet<File> directoryInputFiles) {
        def project = fastdexVariant.project
        long start = System.currentTimeMillis()
        for (File classpathFile : directoryInputFiles) {
            project.logger.error("====fastdex inject dir: ${classpathFile.getAbsolutePath()}====")
            ClassInject.injectDirectory(fastdexVariant,classpathFile,true)
        }
        long end = System.currentTimeMillis()
        project.logger.error("==fastdex inject complete dir-size: ${directoryInputFiles.size()} , use: ${end - start}ms")
    }

    /**
     * 注入所有的依赖的library输出jar
     *
     * @param fastdexVariant
     * @param directoryInputFiles
     */
    static void injectJarInputFiles(FastdexVariant fastdexVariant, HashSet<File> jarInputFiles) {
        if (jarInputFiles == null || jarInputFiles.isEmpty()) {
            return
        }
        def project = fastdexVariant.project
        long start = System.currentTimeMillis()

        Set<LibDependency> libraryDependencies = fastdexVariant.getLibraryDependencies()

        if (DEBUG_INJECT) {
            project.logger.error("==jarInputFiles: " + jarInputFiles)
            project.logger.error("==libraryDependencies: " + libraryDependencies)
        }
        List<File> projectJarFiles = new ArrayList<>()
        //获取所有依赖工程的输出jar (compile project(':xxx'))
        for (LibDependency dependency : libraryDependencies) {
            projectJarFiles.add(dependency.jarFile)
        }
        if (fastdexVariant.configuration.debug) {
            project.logger.error("==fastdex projectJarFiles : ${projectJarFiles}")
        }
        for (File file : jarInputFiles) {
            if (!projectJarFiles.contains(file)) {
                continue
            }
            project.logger.error("==fastdex inject jar: ${file}")
            ClassInject.injectJar(fastdexVariant,file,file)
        }
        long end = System.currentTimeMillis()
        project.logger.error("==fastdex inject complete jar-size: ${projectJarFiles.size()} , use: ${end - start}ms")
    }

    /**
     * 注入jar包
     * @param fastdexVariant
     * @param inputJar
     * @param outputJar
     * @return
     */
    static injectJar(FastdexVariant fastdexVariant, File inputJar,File outputJar) {
        File tempDir = new File(fastdexVariant.buildDir,"temp")
        FileUtils.deleteDir(tempDir)
        FileUtils.ensumeDir(tempDir)

        def project = fastdexVariant.project
        project.copy {
            from project.zipTree(inputJar)
            into tempDir
        }
        ClassInject.injectDirectory(fastdexVariant,tempDir,false)
        project.ant.zip(baseDir: tempDir, destFile: outputJar)
        FileUtils.deleteDir(tempDir)
    }

    /**
     * 注入指定目录下的所有class
     * @param classpath
     */
    static void injectDirectory(FastdexVariant fastdexVariant,File classesDir,boolean applicationProjectSrc) {
        if (!FileUtils.dirExists(classesDir.absolutePath)) {
            return
        }
        Path classpath = classesDir.toPath()

        Files.walkFileTree(classpath,new SimpleFileVisitor<Path>(){
            @Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File classFile = file.toFile()
                String fileName = classFile.getName()
                if (!fileName.endsWith(Constants.CLASS_SUFFIX)) {
                    return FileVisitResult.CONTINUE
                }

                boolean needInject = true
                //
//                if (applicationProjectSrc && (fileName.endsWith("R.class") || fileName.matches("R\\\$\\S{1,}.class"))) {
//                    String packageName = fastdexVariant.getOriginPackageName()
//                    String packageNamePath = packageName.split("\\.").join(File.separator)
//                    if (!classFile.absolutePath.endsWith("${packageNamePath}${File.separator}${fileName}")) {
//                        needInject = false
//                    }
//                }
                if (needInject) {
                    fastdexVariant.project.logger.error("==fastdex inject: ${classFile.getAbsolutePath()}")
                    byte[] classBytes = FileUtils.readContents(classFile)
                    classBytes = ClassInject.inject(classBytes)
                    FileUtils.write2file(classBytes,classFile)
                }
                return FileVisitResult.CONTINUE
            }
        })
    }

    /**
     * 往class字节码注入code
     * @param classBytes
     * @return
     */
    static final byte[] inject(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes)
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        ClassVisitor classVisitor = new MyClassVisitor(classWriter)
        classReader.accept(classVisitor, Opcodes.ASM5)

        return classWriter.toByteArray()
    }

    private static class MyClassVisitor extends ClassVisitor {
        MyClassVisitor(ClassVisitor classVisitor) {
            super(Opcodes.ASM5, classVisitor)
        }

        @Override
        MethodVisitor visitMethod(int access,
                                         String name,
                                         String desc,
                                         String signature,
                                         String[] exceptions) {
            if ("<init>".equals(name)) {
                //get origin method
                MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions)
                MethodVisitor newMethod = new AsmMethodVisit(mv)
                return newMethod
            } else {
                return super.visitMethod(access, name, desc, signature, exceptions)
            }
        }
    }

    static class AsmMethodVisit extends MethodVisitor {
        AsmMethodVisit(MethodVisitor mv) {
            super(Opcodes.ASM5, mv)
        }

        @Override
        void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                super.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;")
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false)
                Label l0 = new Label()
                super.visitJumpInsn(IFEQ, l0)
                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
                mv.visitFieldInsn(GETSTATIC, "fastdex/runtime/antilazyload/AntilazyLoad", "str", "Ljava/lang/String;")
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
                super.visitLabel(l0)
            }
            super.visitInsn(opcode)
        }
    }
}
