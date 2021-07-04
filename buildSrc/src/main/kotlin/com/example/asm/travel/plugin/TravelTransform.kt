package com.example.asm.travel.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager

class TravelTransform : Transform() {

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        transformInvocation?.let {
            transformInvocation.outputProvider.deleteAll()
            transformInvocation.inputs.forEach { transformInput ->
                transformInput.jarInputs.forEach { jarInput ->
                    TransformHelper.transformJars(
                        jarInput,
                        transformInvocation.outputProvider,
                        transformInvocation.isIncremental
                    )
                }
                transformInput.directoryInputs.forEach { directoryInput ->
                    TransformHelper.transformDirectory(
                        directoryInput,
                        transformInvocation.outputProvider,
                        transformInvocation.isIncremental
                    )
                }
            }
        }
    }

    override fun getName(): String {
        return "AsmTravel"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return true
    }

}