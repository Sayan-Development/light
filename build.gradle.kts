plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
    `maven-publish`
}

group = "org.sayandev"
version = "1.0-SNAPSHOT"

allprojects {
    plugins.apply("java")
    plugins.apply("maven-publish")

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = rootProject.group as String

                from(components["java"])
            }
        }
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.sayandev.org/snapshots")
    }
}

tasks.jar {
    manifest {
        attributes(
                "Main-Class" to "org.sayandev.MainKt",
                "Agent-Class" to "org.sayandev.Agent",
                "Can-Redefine-Classes" to "true",
                "Can-Retransform-Classes" to "true"
        )
    }
}

kotlin {
    jvmToolchain(17)
}