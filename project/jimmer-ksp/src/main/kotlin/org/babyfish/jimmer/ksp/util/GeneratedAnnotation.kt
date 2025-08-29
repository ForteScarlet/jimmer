package org.babyfish.jimmer.ksp.util

import love.forte.codegentle.common.code.emitName
import love.forte.codegentle.common.code.emitString
import love.forte.codegentle.common.naming.ClassName
import love.forte.codegentle.common.ref.AnnotationRef
import love.forte.codegentle.common.ref.addMember
import love.forte.codegentle.common.ref.annotationRef
import org.babyfish.jimmer.dto.compiler.DtoFile
import org.babyfish.jimmer.ksp.immutable.generator.GENERATED_BY_CLASS_NAME
import org.babyfish.jimmer.ksp.immutable.meta.ImmutableType

internal fun generatedAnnotation(): AnnotationRef = GENERATED_BY_CLASS_NAME.annotationRef()

//internal fun generatedAnnotation(className: ClassName): AnnotationRef =
//    KotlinAnnotationTypeSpec(GENERATED_BY_CLASS_NAME) {
//
//    }
//        .addMember("type = %T::class", className)
//        .build()
//
//internal fun generatedAnnotation(type: ImmutableType): AnnotationSpec =
//    generatedAnnotation(type.className)
//
//fun generatedAnnotation(dtoFile: DtoFile, mutable: Boolean): AnnotationSpec =
//    AnnotationSpec
//        .builder(GENERATED_BY_CLASS_NAME)
//        .addMember(
//            "file = %S, prompt = %S",
//            dtoFile.path,
//            if (mutable) {
//                "The current DTO type is mutable. If you need to make it immutable, " +
//                        "please remove the ksp argument `jimmer.dto.mutable`"
//            } else {
//                "The current DTO type is immutable. If you need to make it mutable, " +
//                        "please set the ksp argument `jimmer.dto.mutable` to the string \"text\""
//            }
//        )
//        .build()

internal fun generatedAnnotation(className: ClassName): AnnotationRef =
    GENERATED_BY_CLASS_NAME.annotationRef {
        addMember("type", "%V::class") {
            emitName(className)
        }
    }

internal fun generatedAnnotation(type: ImmutableType): AnnotationRef =
    generatedAnnotation(type.className)

fun generatedAnnotation(dtoFile: DtoFile, mutable: Boolean): AnnotationRef =
    GENERATED_BY_CLASS_NAME.annotationRef {
        addMember("file", "%V") {
            emitString(dtoFile.path)
        }

        addMember("prompt", "%V") {
            emitString(
                if (mutable) {
                    "The current DTO type is mutable. If you need to make it immutable, " +
                            "please remove the ksp argument `jimmer.dto.mutable`"
                } else {
                    "The current DTO type is immutable. If you need to make it mutable, " +
                            "please set the ksp argument `jimmer.dto.mutable` to the string \"text\""
                }
            )
        }
    }

//    AnnotationSpec
//        .builder(GENERATED_BY_CLASS_NAME)
//        .addMember(
//            "file = %S, prompt = %S",
//            dtoFile.path,
//            if (mutable) {
//                "The current DTO type is mutable. If you need to make it immutable, " +
//                        "please remove the ksp argument `jimmer.dto.mutable`"
//            } else {
//                "The current DTO type is immutable. If you need to make it mutable, " +
//                        "please set the ksp argument `jimmer.dto.mutable` to the string \"text\""
//            }
//        )
//        .build()
