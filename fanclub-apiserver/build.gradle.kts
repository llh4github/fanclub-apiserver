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
