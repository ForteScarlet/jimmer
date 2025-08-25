plugins {
    `kotlin-convention`
    `dokka-convention`
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(projects.jimmerCore)
    implementation(projects.jimmerDtoCompiler)
    implementation(libs.ksp.symbolProcessing.api)
    implementation(libs.kotlinpoet)
    // TODO remove
    implementation(libs.kotlinpoet.ksp)

    implementation(libs.codegentle.kotlin)
    implementation(libs.codegentle.kotlin.ksp)

    implementation(libs.javax.validation.api)
    implementation(libs.jakarta.validation.api)
}
