package fastdex.build.transform

import com.android.build.api.transform.*
import fastdex.build.util.Constants
import fastdex.build.variant.FastdexVariant
import org.gradle.api.file.FileCollection
import java.lang.reflect.Constructor

/**
 * Created by tong on 17/10/31.
 */
class FastdexDexMergerTransform extends TransformProxy {

    FastdexDexMergerTransform(Transform base,File streamOutputFolder, FastdexVariant fastdexVariant) {
        super(replaceBaseTransform(base,fastdexVariant),streamOutputFolder,fastdexVariant)
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, IOException, InterruptedException {
        if (fastdexVariant.hasDexCache) {
            if (fastdexVariant.projectSnapshoot.diffResultSet.isJavaFileChanged()) {
                File patchJar = new File(fastdexVariant.dexBuilderOutputFolder,Constants.PATCH_JAR)
                fastdexBuilder.patchBuild(base,patchJar,streamOutputFolder)
            }
            else {
                project.logger.error("==fastdex no java files have changed, just ignore")
            }
        }
        else {
            project.logger.error("\n==fastdex normal transform start")
            fastdexBuilder.invokeNormalBuildTransform(base,transformInvocation)
            project.logger.error("==fastdex normal transform end\n")
        }
    }

    static Transform replaceBaseTransform(Transform base, FastdexVariant fastdexVariant) {
        //multiDexEnabled true, minSdkVersion 15
//        base.dexingType: LEGACY_MULTIDEX
//        base.mainDexListFile: file collection
//        base.errorReporter: com.android.build.gradle.internal.ExtraModelInfo@5b238f1d
//        base.dexMerger: DX
//        base.minSdkVersion: 15
//        base.isDebuggable: true
//

        //multiDexEnabled true, minSdkVersion 21
//        base.dexingType: NATIVE_MULTIDEX
//        base.mainDexListFile: null
//        base.errorReporter: com.android.build.gradle.internal.ExtraModelInfo@3c9d924
//        base.dexMerger: DX
//        base.minSdkVersion: 21
//        base.isDebuggable: true

        //com.android.build.gradle.internal.transforms.DexMergerTransform

        Class dexingTypeClass = Class.forName("com.android.builder.dexing.DexingType")
        Object[] values = dexingTypeClass.getMethod("values").invoke(null,null)

        Constructor<?>[] constructors = base.getClass().getConstructors()
        Constructor targetConstructor = constructors[0]

        Transform result =
                targetConstructor.newInstance(values.find { it.isMultiDex() && it.isPreDex() }
                        ,(FileCollection)null
                        , base.errorReporter
                        ,base.dexMerger
                        ,21
                        ,true)
        return result
    }
}