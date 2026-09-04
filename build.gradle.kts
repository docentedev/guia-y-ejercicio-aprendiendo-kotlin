plugins {
    kotlin("jvm") version "2.4.10"
}

group = "com.jumbo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

kotlin {
    jvmToolchain(26)
}

tasks.test {
    useJUnitPlatform()
}