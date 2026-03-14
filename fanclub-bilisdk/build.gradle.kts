plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
//    id("org.graalvm.buildtools.native")
}

description = "fanclub-bilisdk"

dependencies {
    compileOnly(libs.oshai)
    testImplementation(libs.oshai)
    compileOnly(project(":fanclub-common"))
    compileOnly("com.github.ben-manes.caffeine:caffeine")
    compileOnly("org.springframework.boot:spring-boot-starter-cache")
    compileOnly("org.springframework.boot:spring-boot-starter-data-redis")
    compileOnly("org.springframework.boot:spring-boot-starter-jackson")
    testImplementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation(platform(libs.okhttp.bom))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:okhttp-brotli")
    compileOnly("tools.jackson.module:jackson-module-kotlin")
    testImplementation("tools.jackson.module:jackson-module-kotlin")
    implementation("com.squareup.okhttp3:logging-interceptor")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
