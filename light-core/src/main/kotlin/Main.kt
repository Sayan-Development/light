package org.sayandev

import com.google.gson.JsonParser
import org.sayandev.repository.MavenDependency
import org.sayandev.repository.MavenRepository
import org.sayandev.repository.VersionedMavenDependency
import java.io.File

fun main() {
    val repository = MavenRepository("https://repo.sayandev.org/snapshots")
    val dependency = MavenDependency("org.sayandev", "stickynote-core")
    val version = dependency.lastVersion(repository)
    val versionedDependency = VersionedMavenDependency(dependency, version)
    versionedDependency.download(repository, File("./downloads").apply { this.mkdirs() })
    println(versionedDependency.dependency.versionMetaURI(repository, version))
    val transitiveDependencies = versionedDependency.transitiveDependencies(repository)
    for (transitiveDependency in transitiveDependencies) {
        transitiveDependency.download(repository, File("./downloads/transitive").apply { this.mkdirs() })
        println(transitiveDependency)
    }

    val classLoader = ClassLoader.getSystemClassLoader()
    val method = classLoader::class.java.getDeclaredMethod("appendToClassPathForInstrumentation", String::class.java)
    method.isAccessible = true
    for (file in File("./downloads").walk()) {
        method.invoke(classLoader, file.path)
    }
    println(JsonParser.parseString("{\"foo\":\"bar\"}"))
}