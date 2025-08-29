package org.babyfish.jimmer.ksp.util

import com.google.devtools.ksp.symbol.AnnotationUseSiteTarget
import love.forte.codegentle.kotlin.ref.KotlinAnnotationUseSite

fun AnnotationUseSiteTarget.toPoetTarget(): KotlinAnnotationUseSite =
    when (this) {
        AnnotationUseSiteTarget.FIELD -> KotlinAnnotationUseSite.FIELD
        AnnotationUseSiteTarget.GET -> KotlinAnnotationUseSite.GET
        AnnotationUseSiteTarget.SET -> KotlinAnnotationUseSite.SET
        AnnotationUseSiteTarget.PROPERTY -> KotlinAnnotationUseSite.PROPERTY
        AnnotationUseSiteTarget.PARAM -> KotlinAnnotationUseSite.PARAM
        AnnotationUseSiteTarget.SETPARAM -> KotlinAnnotationUseSite.SETPARAM
        AnnotationUseSiteTarget.RECEIVER -> KotlinAnnotationUseSite.RECEIVER
        AnnotationUseSiteTarget.DELEGATE -> KotlinAnnotationUseSite.DELEGATE
        AnnotationUseSiteTarget.FILE -> KotlinAnnotationUseSite.FILE
    }
