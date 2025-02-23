package org.sayandev.light

import org.sayandev.BaseLibrary
import java.io.File

class RequiredTorches(outputDirectory: File): List<BaseLibrary> by listOf(
    BaseLibrary(
        "https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/1.10.1/kotlinx-coroutines-core-jvm-1.10.1.jar",
        File(outputDirectory, "kotlinx-coroutines-core-jvm-1.10.1.jar")
    ),
    BaseLibrary(
        "https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar",
        File(outputDirectory, "asm-9.7.1.jar")
    ),
    BaseLibrary(
        "https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/9.7.1/asm-commons-9.7.1.jar",
        File(outputDirectory, "asm-commons-9.7.1.jar")
    ),
    BaseLibrary(
        "https://repo1.maven.org/maven2/net/thauvin/erik/urlencoder/urlencoder-lib-jvm/1.6.0/urlencoder-lib-jvm-1.6.0.jar",
        File(outputDirectory, "urlencoder-lib-jvm-1.6.0.jar")
    ),
    BaseLibrary(
        "https://repo1.maven.org/maven2/it/krzeminski/snakeyaml-engine-kmp-jvm/3.1.1/snakeyaml-engine-kmp-jvm-3.1.1.jar",
        File(outputDirectory, "snakeyaml-engine-kmp-jvm-3.1.1.jar")
    ),
    BaseLibrary(
        "https://repo1.maven.org/maven2/com/squareup/okio/okio-jvm/3.10.2/okio-jvm-3.10.2.jar",
        File(outputDirectory, "okio-jvm-3.10.2.jar")
    ),
    BaseLibrary(
        "https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-serialization-core-jvm/1.8.0/kotlinx-serialization-core-jvm-1.8.0.jar",
        File(outputDirectory, "kotlinx-serialization-core-jvm-1.8.0.jar")
    ),
    BaseLibrary(
        "https://repo1.maven.org/maven2/com/charleskorn/kaml/kaml-jvm/0.72.0/kaml-jvm-0.72.0.jar",
        File(outputDirectory, "kaml-jvm-0.72.0.jar")
    ),
) {

    fun download(): RequiredTorches {
        for (library in this) {
            library.download()
        }
        return this
    }

    fun load(): RequiredTorches {
        for (library in this) {
            KClassLoaderManager.load(library.outputFile)
        }
        return this
    }
}