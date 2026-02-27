import java.time.Instant

plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.10"
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.4"
    alias(libs.plugins.gradle.git.properties)
    alias(libs.plugins.ksp)
}

group = "llh"
version = project.file("VERSION").readText().trim()
description = "fanclub-vup"

dependencies {
    implementation(libs.oshai)
    implementation(libs.yitter.idgenerator)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
gitProperties {
    dateFormat = "yyyy-MM-dd'T'HH:mmZ"
    keys = listOf("git.branch", "git.commit.id.abbrev", "git.commit.time")
    customProperty("build.time", Instant.now().toString())
    failOnNoGitDirectory = false
}
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}


tasks.withType<Test> {
    useJUnitPlatform()
}

// 配置资源处理，启用过滤器替换 @project.version@
tasks.withType<ProcessResources> {
    filter(org.apache.tools.ant.filters.ReplaceTokens::class, "tokens" to mapOf("project.version" to project.version))
}

repositories {
    // 阿里云Maven镜像（推荐）
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    // 腾讯云Maven镜像
    maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
    // 华为云Maven镜像
    maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
    // 中科大Maven镜像
    maven { url = uri("https://mirrors.ustc.edu.cn/maven/repository/maven-public/") }
    // 原始Maven中央仓库（备用）
    mavenCentral()
}
