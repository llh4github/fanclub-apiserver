plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

description = "fanclub-bilibili"

dependencies {
    implementation(project(":fanclub-common"))

    // HTTP Client & WebSocket (okhttp-brotli 已包含 Brotli 解码支持)
    implementation(platform(libs.okhttp.bom))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:okhttp-brotli")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // JSON
    implementation("tools.jackson.module:jackson-module-kotlin:3.0.0")

    // Logging
    implementation(libs.oshai)
    testImplementation(libs.slf4j.simple)

    // Test
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.coroutines)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
