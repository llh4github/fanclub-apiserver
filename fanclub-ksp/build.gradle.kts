plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
}

description = "fanclub-ksp"

dependencies {
    implementation(libs.ksp)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
}
