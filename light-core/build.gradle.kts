plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

group = "org.sayandev"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "sayandevelopment-snapshots-repo"
        url = uri("https://repo.sayandev.org/snapshots")
    }
}

dependencies {
    compileOnly("com.charleskorn.kaml:kaml:0.72.0")
    compileOnly("com.google.code.gson:gson:2.12.1")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    compileOnly("org.ow2.asm:asm:9.7.1")
    compileOnly("org.ow2.asm:asm-commons:9.7.1")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "org.sayandev.MainJava"
        )
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}