plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
    id("io.spring.dependency-management")
}

description = "fanclub-ksp"

dependencies {
    compileOnly(libs.ksp.api)
    compileOnly(libs.ksp)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    compileOnly("org.springframework.boot:spring-boot-starter-webmvc")
}
