/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

import java.time.Instant

val springAiVersion by extra("2.0.0-M4")
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.graalvm.buildtools.native")
    alias(libs.plugins.gradle.git.properties)
    alias(libs.plugins.ksp)
}

description = "fanclub-apiserver"

configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
}

dependencies {
    ksp(libs.jimmer.ksp)
    ksp(project(":fanclub-ksp"))

    implementation(libs.jsoup)
    implementation(libs.oshai)
    implementation(libs.classgraph)
    implementation(libs.springdoc.ui)
    implementation(libs.yitter.idgenerator)
    implementation(project(":fanclub-bilibili"))
    implementation(project(":fanclub-common"))
    implementation(project(":fanclub-ksp"))
    implementation(libs.bundles.jjwt)

    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    implementation(libs.easy.captcha)
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.security:spring-security-test")

    implementation(platform(libs.okhttp.bom))
    implementation("com.squareup.okhttp3:okhttp")

    implementation(libs.ehcahe)
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation(libs.jimmer.springboot)
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:12.3.0")
    runtimeOnly("org.postgresql:postgresql")

    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    runtimeOnly("org.springframework.boot:spring-boot-docker-compose")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}
// 配置 KSP (如果有)
ksp {
    arg("springAot", "true")
}

graalvmNative {
    binaries {
        named("main") {
            buildArgs.add("--initialize-at-build-time=java.awt.Toolkit,java.awt.GraphicsEnvironment")
            buildArgs.add("--allow-incomplete-classpath")
            // 性能和兼容性优化
            buildArgs.add("--enable-url-protocols=http,https")
            buildArgs.add("--enable-all-security-services")
            buildArgs.add("--no-fallback")
            buildArgs.addAll(
                "--report-unsupported-elements-at-runtime",  // 报告不支持的运行时元素
                "--no-fallback",  // 禁用回退模式，强制暴露问题
                "--verbose",  // 输出详细日志
                "-H:TraceClassInitialization=true",// 跟踪类初始化
                "-H:+ReportExceptionStackTraces",  // 报告异常堆栈
                "-H:+ReportUnsupportedElementsAtRuntime",  // 报告不支持的运行时元素
                "-H:+PrintClassInitialization",  // 打印类初始化信息
                "-H:+AddAllCharsets"  // 包含所有字符集
            )
            resources.autodetect()
        }
    }
}

sourceSets.main {
    kotlin.srcDir("build/generated/ksp/main/kotlin")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// 配置资源处理，启用过滤器替换 @project.version@
tasks.withType<ProcessResources> {
    filter(
        org.apache.tools.ant.filters.ReplaceTokens::class,
        "tokens" to mapOf("project.version" to project.version)
    )
}

gitProperties {
    dateFormat = "yyyy-MM-dd'T'HH:mmZ"
    keys = listOf("git.branch", "git.commit.id.abbrev", "git.commit.time")
    customProperty("build.time", Instant.now().toString())
    failOnNoGitDirectory = false
}
