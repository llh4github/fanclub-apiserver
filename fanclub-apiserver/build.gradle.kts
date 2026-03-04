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

description = "fanclub-apiserver"

configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
}

dependencies {
    implementation(libs.oshai)
    implementation(libs.classgraph)
    implementation(libs.springdoc.ui)
    implementation(libs.yitter.idgenerator)
    implementation(project(":fanclub-bilisdk"))
    implementation(project(":fanclub-common"))
    implementation(libs.bundles.jjwt)

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.security:spring-security-test")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    ksp(libs.jimmer.ksp)
    implementation(libs.jimmer.springboot)
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

graalvmNative {
    binaries {
        named("main") {

            // 性能和兼容性优化
            buildArgs.add("--enable-url-protocols=http,https")
            buildArgs.add("--enable-all-security-services")
            buildArgs.add("--no-fallback")
            
            // 运行时报告不支持元素
            buildArgs.add("--report-unsupported-elements-at-runtime")
        }
    }
}

sourceSets.main {
    kotlin.srcDir("build/generated/ksp/main/kotlin")
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
