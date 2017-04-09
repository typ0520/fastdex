package com.dx168.fastdex.build.util

import org.gradle.api.Project
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
     * 往所有项目代码里注入解决pre-verify问题的code
     * @param directoryInputFiles
     */
    public static final void injectDirectoryInputFiles(Project project,Set<File> directoryInputFiles) {
        long start = System.currentTimeMillis()
        for (File classpathFile : directoryInputFiles) {
            Path classpath = classpathFile.toPath()
            project.logger.error("====fastdex inject dir: ${classpath.toFile().getAbsolutePath()}====")
            Files.walkFileTree(classpath,new SimpleFileVisitor<Path>(){
                @Override
                FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.toFile().getName().endsWith(Constant.CLASS_SUFFIX)) {
                        return FileVisitResult.CONTINUE;
                    }
                    project.logger.error("==fastdex inject: ${file.toFile().getAbsolutePath()}")
                    byte[] classBytes = FileUtils.readContents(file.toFile())
                    classBytes = ClassInject.inject(classBytes)
                    FileUtils.write2file(classBytes,file.toFile())
                    return FileVisitResult.CONTINUE
                }
            })
        }
        long end = System.currentTimeMillis()
        project.logger.error("==fastdex inject complete dir-size: ${directoryInputFiles.size()} , use: ${end - start}ms")
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
                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitFieldInsn(GETSTATIC, "com/dx168/fastdex/runtime/antilazyload/AntilazyLoad", "str", "Ljava/lang/String;");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            }
            super.visitInsn(opcode);
        }
    }
}
