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
    compileOnly(project(":fanclub-common"))
    testImplementation(project(":fanclub-common"))

    implementation(libs.ehcahe)

    // HTTP Client & WebSocket (okhttp-brotli 已包含 Brotli 解码支持)
    implementation(platform(libs.okhttp.bom))
    compileOnly("com.squareup.okhttp3:okhttp")
    testImplementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:okhttp-brotli")
    implementation("com.squareup.okhttp3:logging-interceptor")


    compileOnly("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter")
    // JSON
    compileOnly("tools.jackson.module:jackson-module-kotlin")
    testImplementation("tools.jackson.module:jackson-module-kotlin")

    // Logging
    compileOnly(libs.oshai)
    testImplementation(libs.oshai)
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

// 禁用 bootJar，使用普通 jar 打包（库模块）
tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
