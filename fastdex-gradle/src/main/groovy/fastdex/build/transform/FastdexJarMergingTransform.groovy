package fastdex.build.transform

import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import fastdex.build.util.Constants
import fastdex.build.util.GradleUtils
import fastdex.build.util.JarOperation
import fastdex.build.variant.FastdexVariant
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
                FileUtils.cleanDir(streamOutputFolder)

                //补丁jar
                File patchJar = new File(streamOutputFolder,Constants.PATCH_JAR)
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
}