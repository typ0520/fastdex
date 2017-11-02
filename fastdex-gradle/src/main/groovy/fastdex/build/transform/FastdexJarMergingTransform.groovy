package fastdex.build.transform

import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import fastdex.build.util.Constants
import fastdex.build.util.GradleUtils
import fastdex.build.util.JarOperation
import fastdex.build.variant.FastdexVariant
import com.android.build.api.transform.Format
import fastdex.common.utils.FileUtils

/**
 * 拦截transformClassesWithJarMergingFor${variantName}任务,
 * Created by tong on 17/27/3.
 */
class FastdexJarMergingTransform extends TransformProxy {

    FastdexJarMergingTransform(Transform base,File streamOutputFolder, FastdexVariant fastdexVariant) {
        super(base,streamOutputFolder,fastdexVariant)
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
            fastdexBuilder.injectInputAndSaveClassPath(transformInvocation)

            if (GradleUtils.getAndroidGradlePluginVersion().compareTo(Constants.MIN_BUILD_CACHE_ENABLED_VERSION) >= 0) {
                //不做合并时为了使用build-cache
                fastdexVariant.transformInvocation = transformInvocation
            }
            else {
                base.transform(transformInvocation)
            }
        }
    }

    /**
     * 获取输出jar路径
     * @param invocation
     * @return
     */
    def getCombinedJarFile(TransformInvocation invocation) {
        def outputProvider = invocation.getOutputProvider()

        // all the output will be the same since the transform type is COMBINED.
        // and format is SINGLE_JAR so output is a jar
        File jarFile = outputProvider.getContentLocation("combined", base.getOutputTypes(), base.getScopes(), Format.JAR)
        FileUtils.ensumeDir(jarFile.getParentFile())
        return jarFile
    }
}