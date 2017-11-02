package fastdex.build.transform

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.gradle.api.ApplicationVariant
import fastdex.build.variant.FastdexBuilder
import fastdex.build.variant.FastdexVariant
import org.gradle.api.Project

/**
 * Created by tong on 17/10/3.
 */
class TransformProxy extends Transform {
    final Transform base
    final File streamOutputFolder
    final FastdexVariant fastdexVariant
    final FastdexBuilder fastdexBuilder
    final Project project
    final String variantName
    final ApplicationVariant androidVariant

    TransformProxy(Transform base,File streamOutputFolder,FastdexVariant fastdexVariant) {
        this.base = base
        this.streamOutputFolder = streamOutputFolder
        this.fastdexVariant = fastdexVariant
        this.fastdexBuilder = fastdexVariant.fastdexBuilder
        this.project = fastdexVariant.project
        this.variantName = fastdexVariant.variantName
        this.androidVariant = fastdexVariant.androidVariant
    }

    @Override
    String getName() {
        return base.getName()
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return base.getInputTypes()
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return base.getScopes()
    }

    @Override
    boolean isIncremental() {
        return base.isIncremental()
    }
}