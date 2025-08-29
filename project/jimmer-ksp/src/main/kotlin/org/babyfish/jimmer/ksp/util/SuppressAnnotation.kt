package org.babyfish.jimmer.ksp.util

import love.forte.codegentle.common.naming.toClassName
import love.forte.codegentle.common.ref.AnnotationRef
import love.forte.codegentle.common.ref.annotationRef

//internal fun suppressAllAnnotation() = AnnotationSpec
//    .builder(Suppress::class)
//    .addMember("\"warnings\"")
//    .build()

internal fun suppressAllAnnotation(): AnnotationRef =
    Suppress::class.toClassName().annotationRef {
        addMember("value", "\"warnings\"")
    }
