package fastdex.build.transform

import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import fastdex.build.util.Constants
import fastdex.build.util.FastdexUtils
import fastdex.build.util.JarOperation
import fastdex.build.variant.FastdexVariant
import fastdex.common.utils.FileUtils

/**
 * Created by tong on 17/11/2.
 */
class FastdexPreDexTransform extends TransformProxy {

    FastdexPreDexTransform(Transform base,File streamOutputFolder, FastdexVariant fastdexVariant) {
        super(base,streamOutputFolder,fastdexVariant)
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, IOException, InterruptedException {
        if (fastdexVariant.hasDexCache) {
            project.logger.error("\n==fastdex patch transform start,we will generate dex file")

            if (base.dexingType.isMultiDex() && base.dexingType.isPreDex()) {
                if (fastdexVariant.projectSnapshoot.diffResultSet.isJavaFileChanged()) {
                    //生成补丁jar包
                    File patchJar = new File(FastdexUtils.getBuildDir(project,variantName),Constants.PATCH_JAR)
                    JarOperation.generatePatchJar(fastdexVariant,transformInvocation,patchJar)
                    fastdexBuilder.patchBuild(base,patchJar,streamOutputFolder)
                }
                else {
                    project.logger.error("==fastdex no java files have changed, just ignore")
                }
            }
            else {
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
        }
        else {
            if (base.dexingType.isMultiDex() && base.dexingType.isPreDex()) {
                fastdexBuilder.injectInputAndSaveClassPath(transformInvocation)
                fastdexBuilder.invokeNormalBuildTransform(base,transformInvocation)
            }
            else {
                fastdexBuilder.injectInputAndSaveClassPath(transformInvocation)
                base.transform(transformInvocation)
            }
        }
    }
}
