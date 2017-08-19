package fastdex.build.transform

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import fastdex.build.util.ClassInject
import fastdex.build.util.Constants
import fastdex.build.util.FastdexUtils
import fastdex.build.util.JarOperation
import fastdex.build.variant.FastdexVariant
import com.android.build.api.transform.Format
import fastdex.common.utils.FileUtils
import fastdex.common.utils.SerializeUtils

/**
 * 拦截transformClassesWithJarMergingFor${variantName}任务,
 * Created by tong on 17/27/3.
 */
class FastdexJarMergingTransform extends TransformProxy {
    FastdexVariant fastdexVariant

    FastdexJarMergingTransform(Transform base, FastdexVariant fastdexVariant) {
        super(base)
        this.fastdexVariant = fastdexVariant
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, IOException, InterruptedException {
        if (fastdexVariant.hasDexCache) {
            if (fastdexVariant.projectSnapshoot.diffResultSet.isJavaFileChanged()) {
                //补丁jar
                File patchJar = getCombinedJarFile(transformInvocation)
                //生成补丁jar
                JarOperation.generatePatchJar(fastdexVariant,transformInvocation,patchJar)
            }
            else {
                fastdexVariant.project.logger.error("==fastdex no java files have changed, just ignore")
            }
        }
        else {
            //所有输入的jar
            Set<String> jarInputFiles = new HashSet<>();
            for (TransformInput input : transformInvocation.getInputs()) {
                Collection<JarInput> jarInputs = input.getJarInputs()
                if (jarInputs != null) {
                    for (JarInput jarInput : jarInputs) {
                        jarInputFiles.add(jarInput.getFile().absolutePath)
                    }
                }
            }
            File classpathFile = new File(FastdexUtils.getBuildDir(fastdexVariant.project,fastdexVariant.variantName),Constants.CLASSPATH_FILENAME)
            SerializeUtils.serializeTo(classpathFile,jarInputFiles)

            //inject dir input
            ClassInject.injectTransformInvocation(fastdexVariant,transformInvocation)
            base.transform(transformInvocation)
        }

        fastdexVariant.executedJarMerge = true
    }

    /**
     * 获取输出jar路径
     * @param invocation
     * @return
     */
    public File getCombinedJarFile(TransformInvocation invocation) {
        def outputProvider = invocation.getOutputProvider();

        // all the output will be the same since the transform type is COMBINED.
        // and format is SINGLE_JAR so output is a jar
        File jarFile = outputProvider.getContentLocation("combined", base.getOutputTypes(), base.getScopes(), Format.JAR);
        FileUtils.ensumeDir(jarFile.getParentFile());
        return jarFile
    }
}