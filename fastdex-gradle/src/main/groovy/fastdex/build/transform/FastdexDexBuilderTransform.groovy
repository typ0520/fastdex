package fastdex.build.transform

import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import fastdex.build.util.Constants
import fastdex.build.util.JarOperation
import fastdex.build.variant.FastdexVariant
import fastdex.common.utils.FileUtils

/**
 * Created by tong on 17/10/31.
 */
class FastdexDexBuilderTransform extends TransformProxy {

    FastdexDexBuilderTransform(Transform base,File streamOutputFolder, FastdexVariant fastdexVariant) {
        super(base,streamOutputFolder,fastdexVariant)
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, IOException, InterruptedException {
        if (fastdexVariant.hasDexCache) {
            project.logger.error("\n==fastdex patch transform start,we will generate dex file")
            if (fastdexVariant.projectSnapshoot.diffResultSet.isJavaFileChanged()) {
                FileUtils.deleteDir(streamOutputFolder)
                File patchJar = new File(streamOutputFolder,Constants.PATCH_JAR)
                //生成补丁jar
                JarOperation.generatePatchJar(fastdexVariant,transformInvocation,patchJar)
            }
            else {
                project.logger.error("==fastdex no java files have changed, just ignore")
            }
        }
        else {
            fastdexBuilder.injectInputAndSaveClassPath(transformInvocation)
            base.transform(transformInvocation)
        }
    }
}
