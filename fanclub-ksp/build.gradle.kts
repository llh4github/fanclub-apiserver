/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
}

description = "fanclub-ksp"

dependencies {
    implementation(libs.ksp)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    compileOnly(project(":fanclub-common"))
}
