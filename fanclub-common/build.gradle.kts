plugins {
    kotlin("jvm")
}

description = "fanclub-common"

dependencies {
    compileOnly(libs.oshai)
    compileOnly(libs.bundles.slf4j)
    testImplementation(libs.oshai)
    testImplementation(libs.bundles.slf4j)
    compileOnly("org.jetbrains.kotlin:kotlin-reflect")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
