package org.babyfish.jimmer.ksp

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isProtected
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import org.babyfish.jimmer.ksp.generator.DraftGenerator
import org.babyfish.jimmer.ksp.meta.Context
import java.util.concurrent.atomic.AtomicBoolean

class ImmutableProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    private val processed = AtomicBoolean()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!processed.compareAndSet(false, true)) {
            return emptyList()
        }
        val ctx = Context(resolver)
        val classDeclarationMultiMap = findModelMap(ctx)
        for ((file, classDeclarations) in classDeclarationMultiMap) {
            DraftGenerator(environment.codeGenerator, ctx, file, classDeclarations)
                .generate(resolver.getAllFiles().toList())
        }
        return classDeclarationMultiMap.values.flatten()
    }

    private fun findModelMap(ctx: Context): Map<KSFile, List<KSClassDeclaration>> {
        val modelMap = mutableMapOf<KSFile, MutableList<KSClassDeclaration>>()
        for (file in ctx.resolver.getAllFiles()) {
            for (classDeclaration in file.declarations.filterIsInstance<KSClassDeclaration>()) {
                val annotation = ctx.typeAnnotationOf(classDeclaration)
                if (classDeclaration.qualifiedName !== null && annotation != null) {
                    if (classDeclaration.classKind != ClassKind.INTERFACE) {
                        throw GeneratorException(
                            "The immutable interface '${classDeclaration.fullName}' " +
                                "must be interface"
                        )
                    }
                    if (classDeclaration.typeParameters.isNotEmpty()) {
                        throw GeneratorException(
                            "The immutable interface '${classDeclaration.fullName}' " +
                                "cannot have type parameters"
                        )
                    }
                    if (classDeclaration.isPrivate() || classDeclaration.isProtected()) {
                        throw GeneratorException(
                            "The immutable interface '${classDeclaration.fullName}' " +
                                "cannot be private or protected'"
                        )
                    }
                    modelMap.computeIfAbsent(file) { mutableListOf() } +=
                        classDeclaration
                }
            }
        }
        return modelMap
    }
}