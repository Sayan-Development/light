package org.sayandev

import org.sayandev.repository.MavenDependency
import org.sayandev.repository.MavenRepository
import org.sayandev.repository.Version
import java.io.File

fun main() {
    val classLoader = ClassLoader.getSystemClassLoader()
    val method = classLoader::class.java.getDeclaredMethod("appendToClassPathForInstrumentation", String::class.java)
    method.isAccessible = true

    val manager = DependencyManager(File("./downloads"))
    manager.addRepository(MavenRepository("https://repo1.maven.org/maven2"))
    manager.addRepository(MavenRepository("https://repo.sayandev.org/snapshots"))

    MavenDependency("org.jetbrains.kotlinx", "kotlinx-coroutines-core", Version("1.10.1")).download(manager, true, false)
    MavenDependency("com.google.guava", "guava", Version("33.4.0-jre")).download(manager, true, false)
    manager.loadDependencies()

    manager.addDependency(MavenDependency("net.mamoe.yamlkt", "yamlkt-jvm", Version("0.13.0")))
    manager.addDependency(MavenDependency("org.jetbrains.kotlin", "kotlin-metadata-jvm", Version("2.1.0")))
    manager.addDependency(MavenDependency("io.ktor", "ktor-server-core-jvm", Version("2.3.12")))
    manager.addDependency(MavenDependency("io.ktor", "ktor-server-netty-jvm", Version("2.3.12")))
    manager.addDependency(MavenDependency("ch.qos.logback", "logback-classic", Version("1.4.14")))
    manager.addDependency(MavenDependency("io.ktor", "ktor-server-config-yaml-jvm", Version("2.3.12")))
    manager.addDependency(MavenDependency("io.ktor", "ktor-server-cors-jvm", Version("2.3.12")))
    manager.addDependency(MavenDependency("io.ktor", "ktor-server-auth-jvm", Version("2.3.12")))
    CoroutineUtils.launch(AsyncDispatcher("main", 1)) {
        manager.downloadDependenciesAsync().await()
        manager.loadDependencies()
    }

    for (file in File("./downloads").walk()) {
        method.invoke(classLoader, file.path)
    }

//    SayanChatServer()

//    println(Yaml.encodeToString(Test.serializer(), Test("test", "optional", Test.Nested(1), listOf("a", "b", "c"))))

//    println("")
//    println(JsonParser.parseString("{\"foo\":\"bar\"}").asJsonObject.get("foo").asString)
}