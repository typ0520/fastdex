package com.dx168.fastdex.build.transform

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform

/**
 * Created by tong on 17/10/3.
 */
public class TransformProxy extends Transform {
    Transform base

    TransformProxy(Transform base) {
        this.base = base
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