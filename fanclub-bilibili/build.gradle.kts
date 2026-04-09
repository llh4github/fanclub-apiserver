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

    // JSON
    implementation("tools.jackson.module:jackson-module-kotlin:3.0.0")

    // Logging
    implementation(libs.oshai)
}
