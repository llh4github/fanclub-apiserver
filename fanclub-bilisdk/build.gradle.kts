import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
//    id("org.graalvm.buildtools.native")
}

description = "fanclub-bilisdk"

dependencies {
    implementation(libs.ehcahe)
    implementation(libs.oshai)
    testImplementation(libs.oshai)
    implementation(project(":fanclub-common"))
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    testImplementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation(platform(libs.okhttp.bom))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("com.squareup.okhttp3:okhttp-brotli")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}
tasks.named<Jar>("jar") {
    enabled = true
}
tasks.named<BootBuildImage>("bootBuildImage") {
    enabled = false
}
