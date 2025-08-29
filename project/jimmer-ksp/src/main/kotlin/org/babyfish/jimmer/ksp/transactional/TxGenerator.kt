package org.babyfish.jimmer.ksp.transactional

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import love.forte.codegentle.common.code.*
import love.forte.codegentle.common.ksp.toClassName
import love.forte.codegentle.common.naming.parseToPackageName
import love.forte.codegentle.common.ref.annotationRef
import love.forte.codegentle.common.ref.ref
import love.forte.codegentle.kotlin.KotlinFile
import love.forte.codegentle.kotlin.KotlinFileBuilder
import love.forte.codegentle.kotlin.addSimpleClassType
import love.forte.codegentle.kotlin.ksp.toAnnotationRef
import love.forte.codegentle.kotlin.ksp.toTypeName
import love.forte.codegentle.kotlin.modifiers
import love.forte.codegentle.kotlin.spec.*
import love.forte.codegentle.kotlin.strategy.DefaultKotlinWriteStrategy
import org.babyfish.jimmer.ksp.*
import org.babyfish.jimmer.ksp.immutable.generator.PROPAGATION_CLASS_NAME
import org.babyfish.jimmer.ksp.util.fastResolve
import java.io.OutputStreamWriter

class TxGenerator(
    private val codeGenerator: CodeGenerator,
    private val ctx: Context,
    private val declaration: KSClassDeclaration
) {
    private val simpleName = declaration.simpleName.asString() + "Tx"

    private val sqlClientName: String =
        determineSqlClientName()

    private val classTx = declaration.annotation(TX)

    fun generate(allFiles: List<KSFile>) {
        codeGenerator.createNewFile(
            Dependencies(false, *allFiles.toTypedArray()),
            declaration.packageName.asString(),
            simpleName
        ).use {

            val fileSpec = KotlinFile(
                declaration.packageName.asString().parseToPackageName(),
            ) {
                // TODO addAnnotation(suppressAllAnnotation())
                addType()
            }

//            val fileSpec = FileSpec
//                .builder(
//                    declaration.packageName.asString(),
//                    simpleName
//                ).apply {
//                    indent("    ")
//                    addAnnotation(suppressAllAnnotation())
//                    addType()
//                }.build()
            val writer = OutputStreamWriter(it, Charsets.UTF_8)
            fileSpec.writeTo(writer, DefaultKotlinWriteStrategy())
            writer.flush()
        }
    }

    private fun determineSqlClientName(): String {
        val sqlClientType =
            ctx.resolver.getClassDeclarationByName("org.babyfish.jimmer.sql.kt.KSqlClient")!!.asStarProjectedType()
        val props = declaration.getDeclaredProperties()
            .filter { sqlClientType.isAssignableFrom(it.type.fastResolve()) }
            .toList()
        if (props.isEmpty()) {
            throw MetaException(
                declaration,
                "The class uses @Tx must have a non-static properties whose type is KSqlClient"
            )
        }
        if (props.size > 1) {
            throw MetaException(
                declaration,
                "The class uses @Tx cannot multiple non-static sqlClient properties"
            )
        }
        val prop = props[0]
        if (prop.isPrivate()) {
            throw MetaException(
                prop,
                "The sqlClient field of the class uses @Tx cannot be private, protected or internal is recommended"
            )
        }
        return prop.name
    }

    private fun KotlinFileBuilder.addType() {
        addSimpleClassType(simpleName) {
            modifiers {
                if (declaration.isInternal()) {
                    internal()
                }
                if (declaration.isAbstract()) {
                    abstract()
                }
                for (anno in declaration.annotations) {
                    val fullName = anno.fullName
                    if (fullName != TX && fullName != TARGET_ANNOTATION) {
                        addAnnotation(anno.toAnnotationRef())
                    }
                }
                val targetAnnotation = declaration.annotation(TARGET_ANNOTATION)
                if (targetAnnotation != null) {
                    val annoDeclaration = targetAnnotation.getClassArgument("value")
                    addAnnotation(annoDeclaration!!.toClassName().annotationRef())
                }
                superclass(declaration.toClassName())
                declaration.primaryConstructor?.let {
                    primaryConstructor {
                        superConstructorDelegation {
                            for (parameter in it.parameters) {
                                this.addArgument(parameter.name!!.asString())
//                                addSuperclassConstructorParameter(parameter.name!!.asString())
                            }
                        }
                    }
                }
                addConstructors()
                addFunctions()
            }
        }
    }

    private fun KotlinSimpleTypeSpec.Builder.addConstructors() {
        val primaryConstructor = declaration.primaryConstructor?.takeIf { !it.isPrivate() }
        if (primaryConstructor !== null) {
            primaryConstructor {
                setConstructorProperties(primaryConstructor, true)
            }

//            primaryConstructor(
//                FunSpec.constructorBuilder().apply {
//                    setConstructorProperties(primaryConstructor, true)
//                }.build()
//            )
        } else {
            for (constructor in declaration.getConstructors()) {
                if (!constructor.isPrivate()) {
                    addSecondaryConstructor {
                        setConstructorProperties(constructor, false)
                    }
                }
//                if (!constructor.isPrivate()) {
//                    addFunction(
//                        FunSpec.constructorBuilder().apply {
//                            setConstructorProperties(constructor, false)
//                        }.build()
//                    )
//                }
            }
        }
    }

    private fun KotlinConstructorSpec.Builder.setConstructorProperties(
        constructor: KSFunctionDeclaration,
        primary: Boolean
    ) {
        modifiers {
            if (constructor.isProtected()) {
                protected()
            }
            if (constructor.isInternal()) {
                internal()
            }
        }

        for (anno in constructor.annotations) {
//            addAnnotation(anno.toAnnotationSpec())
            addAnnotation(anno.toAnnotationRef())
        }
        for (parameter in constructor.parameters) {
            addParameter(parameter.name!!.asString(), parameter.type.toTypeName())
//            addParameter(
//                ParameterSpec
//                    .builder(parameter.name!!.asString(), parameter.type.toTypeName())
//                    .build()
//            )
        }
        if (!primary) {
            superConstructorDelegation {
                addArguments(constructor.parameters.map { CodeValue(it.name!!.asString()) })
            }
//            callSuperConstructor(*constructor.parameters.map { it.name!!.asString() }.toTypedArray())
        }
    }

    private fun KotlinSimpleTypeSpec.Builder.addFunctions() {
        for (function in declaration.getDeclaredFunctions()) {
            val tx = function.annotation(TX)
            if (tx != null && !function.isOpen()) {
                throw MetaException(
                    function,
                    "Only open method cannot be decorated by @Tx"
                )
            }
            val finalTx = when {
                tx !== null -> tx
                classTx === null || !function.isPublic() || function.isConstructor() -> continue
                else -> {
                    if (!function.isOpen()) {
                        throw MetaException(
                            function,
                            "The public method inherits the class-level @Tx must be open"
                        );
                    }
                    classTx
                }
            }
            addFunction(function, finalTx)
        }
    }

    private fun KotlinSimpleTypeSpec.Builder.addFunction(function: KSFunctionDeclaration, tx: KSAnnotation) {
        val propagation = tx.get<Any>("value").toString().let {
            val index = it.lastIndexOf(".")
            if (index == -1) {
                it
            } else {
                it.substring(index + 1)
            }
        }

        addFunction(function.simpleName.asString()) {
            modifiers {
                override()
                if (function.isProtected()) {
                    protected()
                } else if (function.isInternal()) {
                    internal()
                }
                for (anno in function.annotations) {
                    if (anno.fullName != TX) {
                        addAnnotation(anno.toAnnotationRef())
                    }
                }
                for (parameter in function.parameters) {
                    addParameter(parameter.name!!.asString(), parameter.type.toTypeName())
                }
                function.returnType?.let {
                    returns(it.toTypeName().ref())
                }
                addCode {
                    beginControlFlow("return this.%V.transaction(%V.%V)") {
                        emitLiteral(sqlClientName)
                        emitType(PROPAGATION_CLASS_NAME)
                        emitLiteral(propagation)
                    }
                    addStatement("super.%V(%V)") {
                        emitLiteral(function.simpleName.asString())
                        emitLiteral(function.parameters.map { it.name!!.asString() }.joinToString { ", " })
                    }
                    endControlFlow()
                }
            }
        }
    }
}
