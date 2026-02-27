import org.gradle.api.plugins.JavaPluginExtension
import java.time.Instant

plugins {
    kotlin("jvm") version "2.3.10" apply true
    kotlin("plugin.spring") version "2.3.10" apply false
    id("org.springframework.boot") version "4.0.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.graalvm.buildtools.native") version "0.11.4" apply false
    alias(libs.plugins.gradle.git.properties) apply false
    alias(libs.plugins.ksp) apply false
}

group = "llh"
version = project.file("VERSION").readText().trim()
description = "fanclub-vup"

subprojects {

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

    tasks.withType<Test> {
        useJUnitPlatform()
    }
    
    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
            compilerOptions {
                freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
            }
        }
    }
    
    // 配置资源处理，启用过滤器替换 @project.version@
    tasks.withType<ProcessResources> {
        filter(
            org.apache.tools.ant.filters.ReplaceTokens::class,
            "tokens" to mapOf("project.version" to project.version)
        )
    }

    // 仅当项目应用了 Java 插件时，才配置 Java 扩展
    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion = JavaLanguageVersion.of(25)
            }
        }
    }

    // 配置 Git Properties 插件 - 使用属性配置方式
    plugins.withId("com.gorylenko.gradle-git-properties") {
        // 通过项目属性配置
        project.ext.set("gitProperties", mapOf(
            "dateFormat" to "yyyy-MM-dd'T'HH:mmZ",
            "keys" to listOf("git.branch", "git.commit.id.abbrev", "git.commit.time"),
            "customProperty" to mapOf("build.time" to Instant.now().toString()),
            "failOnNoGitDirectory" to false
        ))
    }
}