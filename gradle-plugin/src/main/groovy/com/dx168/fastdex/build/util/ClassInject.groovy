package com.dx168.fastdex.build.util

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.dx168.fastdex.build.variant.FastdexVariant
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
 import com.dx168.fastdex.runtime.antilazyload.AntilazyLoad;

 public class MainActivity {
 public MainActivity() {
 System.out.println(Antilazyload.str);
 }
 }
 ''''''
 * 代码注入，往所有的构造方法中添加对com.dx168.fastdex.runtime.antilazyload.AntilazyLoad的依赖
 * Created by tong on 17/10/3.
 */
public class ClassInject implements Opcodes {
    /**
     * 注入class目录和jar文件
     * @param fastdexVariant
     * @param transformInvocation
     */
    public static void injectTransformInvocation(FastdexVariant fastdexVariant, TransformInvocation transformInvocation) {
        //所有的class目录
        HashSet<File> directoryInputFiles = new HashSet<>();
        //所有输入的jar
        HashSet<File> jarInputFiles = new HashSet<>();
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
    public static void injectDirectoryInputFiles(FastdexVariant fastdexVariant, HashSet<File> directoryInputFiles) {
        def project = fastdexVariant.project
        long start = System.currentTimeMillis()
        for (File classpathFile : directoryInputFiles) {
            project.logger.error("====fastdex ==inject dir: ${classpathFile.getAbsolutePath()}====")
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
    public static void injectJarInputFiles(FastdexVariant fastdexVariant, HashSet<File> jarInputFiles) {
        def project = fastdexVariant.project
        long start = System.currentTimeMillis()

        Set<LibDependency> libraryDependencies = fastdexVariant.libraryDependencies
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
            project.logger.error("==fastdex ==inject jar: ${file}")
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
    public static injectJar(FastdexVariant fastdexVariant, File inputJar,File outputJar) {
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
//        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream()
//
//        ZipOutputStream outputJarStream = null
//        ZipFile zipFile = new ZipFile(file.absolutePath);
//        Enumeration enumeration = zipFile.entries();
//        try {
//            outputJarStream = new ZipOutputStream(new FileOutputStream(new File("/Users/tong/Desktop/${file.name}")));
//            while (enumeration.hasMoreElements()) {
//                ZipEntry entry = (ZipEntry) enumeration.nextElement();
//                if (entry.isDirectory()) {
//                    continue;
//                }
//
//                ZipEntry e = new ZipEntry(entry.name)
//                outputJarStream.putNextEntry(e)
//                //byte[] bytes = FileUtils.readStream(zipFile.getInputStream(entry))
//                byte[] bytes = FileUtils.readContents(new File("/Users/tong/Desktop/a.txt"))
//                outputJarStream.write(bytes,0,bytes.length);
//                outputJarStream.flush()
//                outputJarStream.closeEntry()
//            }
//            //FileUtils.write2file(zipOutputStream.toByteArray(),file);
//        } finally {
//            if (outputJarStream != null) {
//                outputJarStream.close();
//            }
//
//            if (zipFile != null) {
//                zipFile.close();
//            }
//        }
    }

    /**
     * 注入指定目录下的所有class
     * @param classpath
     */
    public static void injectDirectory(FastdexVariant fastdexVariant,File classesDir,boolean applicationProjectSrc) {
        if (!FileUtils.dirExists(classesDir.absolutePath)) {
            return
        }
        Path classpath = classesDir.toPath()

        Files.walkFileTree(classpath,new SimpleFileVisitor<Path>(){
            @Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File classFile = file.toFile()
                String fileName = classFile.getName()
                if (!fileName.endsWith(Constant.CLASS_SUFFIX)) {
                    return FileVisitResult.CONTINUE;
                }

                boolean needInject = true
                if (applicationProjectSrc && (fileName.endsWith("R.class") || fileName.matches("R\\\$\\S{1,}.class"))) {
                    String packageName = fastdexVariant.getApplicationPackageName()
                    String packageNamePath = packageName.split("\\.").join(File.separator)
                    if (!classFile.absolutePath.endsWith("${packageNamePath}${File.separator}${fileName}")) {
                        needInject = false
                    }
                }
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
    public static final byte[] inject(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new MyClassVisitor(classWriter);
        classReader.accept(classVisitor, Opcodes.ASM5);

        return classWriter.toByteArray()
    }

    private static class MyClassVisitor extends ClassVisitor {
        public MyClassVisitor(ClassVisitor classVisitor) {
            super(Opcodes.ASM5, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(int access,
                                         String name,
                                         String desc,
                                         String signature,
                                         String[] exceptions) {
            if ("<init>".equals(name)) {
                //get origin method
                MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
                //System.out.println(name + " | " + desc + " | " + signature);
                MethodVisitor newMethod = new AsmMethodVisit(mv);
                return newMethod;
            } else {
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }
    }

    static class AsmMethodVisit extends MethodVisitor {
        public AsmMethodVisit(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                super.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                Label l0 = new Label();
                super.visitJumpInsn(IFEQ, l0);
                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitFieldInsn(GETSTATIC, "com/dx168/fastdex/runtime/antilazyload/AntilazyLoad", "str", "Ljava/lang/String;");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                super.visitLabel(l0);
            }
            super.visitInsn(opcode);
        }
    }
}
