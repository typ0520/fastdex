package com.dx168.fastdex.build.transform

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.google.common.collect.ImmutableList

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

    /**
     * change the jar file to TransformInputs
     */
    Collection<TransformInput> jarFileToInputs(File jarFile) {
        TransformInput transformInput = new TransformInput() {
            @Override
            Collection<JarInput> getJarInputs() {
                JarInput jarInput = new JarInput() {
                    @Override
                    Status getStatus() {
                        return Status.ADDED
                    }

                    @Override
                    String getName() {
                        return jarFile.getName().substring(0,
                                jarFile.getName().length() - ".jar".length())
                    }

                    @Override
                    File getFile() {
                        return jarFile
                    }

                    @Override
                    Set<QualifiedContent.ContentType> getContentTypes() {
                        return getInputTypes()
                    }

                    @Override
                    Set<QualifiedContent.Scope> getScopes() {
                        return getScopes()
                    }
                }
                return ImmutableList.of(jarInput)
            }


            @Override
            Collection<DirectoryInput> getDirectoryInputs() {
                return ImmutableList.of()
            }
        }
        return ImmutableList.of(transformInput)
    }
}