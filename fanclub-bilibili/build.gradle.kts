/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "fanclub-bilibili"

dependencies {
    implementation(project(":fanclub-common"))

    implementation(libs.ehcahe)

    // HTTP Client & WebSocket (okhttp-brotli 已包含 Brotli 解码支持)
    implementation(platform(libs.okhttp.bom))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:okhttp-brotli")
    implementation("com.squareup.okhttp3:logging-interceptor")

    implementation("org.springframework.boot:spring-boot-starter")
    // JSON
    implementation("tools.jackson.module:jackson-module-kotlin")

    // Logging
    implementation(libs.oshai)
    testImplementation(libs.slf4j.simple)

    // Test
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.coroutines)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
