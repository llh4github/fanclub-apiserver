import java.time.Instant

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.graalvm.buildtools.native")
    alias(libs.plugins.gradle.git.properties)
    alias(libs.plugins.ksp)
}

graalvmNative {
    binaries {
        named("main") {

            buildArgs.add("--initialize-at-build-time=org.springframework.boot.logging.log4j2.SpringBootPropertySource")
            buildArgs.add("--initialize-at-build-time=org.springframework.boot.logging.log4j2.SpringEnvironmentLookup")

            // Log4j2核心组件初始化
            buildArgs.add("--initialize-at-build-time=org.apache.logging.log4j")
            buildArgs.add("--initialize-at-build-time=org.apache.logging.log4j.core")
            buildArgs.add("--initialize-at-build-time=org.apache.logging.log4j.spi")
            
            // SLF4J相关组件初始化
            buildArgs.add("--initialize-at-build-time=org.slf4j")
            buildArgs.add("--initialize-at-build-time=org.slf4j.helpers")
            buildArgs.add("--initialize-at-build-time=org.slf4j.impl")
            
            // Log4j-SLF4J桥接组件
            buildArgs.add("--initialize-at-build-time=org.apache.logging.slf4j.Log4jMarkerFactory")
            buildArgs.add("--initialize-at-build-time=org.apache.logging.slf4j.Log4jLoggerFactory")
            buildArgs.add("--initialize-at-build-time=org.apache.logging.slf4j.SLF4JServiceProvider")
            buildArgs.add("--initialize-at-build-time=org.apache.logging.slf4j.EventDataConverter")
            
            // 具体的工厂和管理器类
            buildArgs.add("--initialize-at-build-time=org.slf4j.MarkerFactory")
            buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory")

            // 性能和兼容性优化
            buildArgs.add("--enable-url-protocols=http,https")
            buildArgs.add("--enable-all-security-services")
            buildArgs.add("--no-fallback")
            
            // 运行时报告不支持元素
            buildArgs.add("--report-unsupported-elements-at-runtime")
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

description = "fanclub-apiserver"

dependencies {
    implementation(libs.oshai)
    implementation(libs.yitter.idgenerator)
    implementation(project(":fanclub-bilisdk"))
    implementation(project(":fanclub-common"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-actuator") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-websocket") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
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
