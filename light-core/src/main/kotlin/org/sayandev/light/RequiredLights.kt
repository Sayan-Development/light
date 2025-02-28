package org.sayandev.light

import org.sayandev.DirectLibrary
import org.sayandev.LightClassLoader
import java.io.File

class RequiredLights(val outputDirectory: File): List<DirectLibrary> by listOf(
    DirectLibrary(
        "https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/1.10.1/kotlinx-coroutines-core-jvm-1.10.1.jar",
        File(outputDirectory, "kotlinx-coroutines-core-jvm-1.10.1.jar"),
        "069c5988633230e074ec0d39321ec3cdaa4547c49e90ba936c63d8fc91c8c00d",
        "SHA-256"
    ),
    // moved to RelocationHelper
    /*BaseLibrary(
        "https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar",
        File(outputDirectory, "asm-9.7.1.jar"),
        "70ef490a486da0997a11ae2b8276718d55997e4872b6afd3ee47e634d139333f",
        "SHA-256"
    ),
    BaseLibrary(
        "https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/9.7.1/asm-commons-9.7.1.jar",
        File(outputDirectory, "asm-commons-9.7.1.jar"),
        "9a579b54d292ad9be171d4313fd4739c635592c2b5ac3a459bbd1049cddec6a0",
        "SHA-256"
    ),*/
    DirectLibrary(
        "https://repo1.maven.org/maven2/net/thauvin/erik/urlencoder/urlencoder-lib-jvm/1.6.0/urlencoder-lib-jvm-1.6.0.jar",
        File(outputDirectory, "urlencoder-lib-jvm-1.6.0.jar"),
        "b2caad7c083e001e21d405aaa6e6bef49445e012f730269b2c18894ec705f86c",
        "SHA-256"
    ),
    DirectLibrary(
        "https://repo1.maven.org/maven2/it/krzeminski/snakeyaml-engine-kmp-jvm/3.1.1/snakeyaml-engine-kmp-jvm-3.1.1.jar",
        File(outputDirectory, "snakeyaml-engine-kmp-jvm-3.1.1.jar"),
        "733e1347e3ec909e8a7ee199e48a64d0180569461b3bc08c8a6bb1335f5ce1bd",
        "SHA-256"
    ),
    DirectLibrary(
        "https://repo1.maven.org/maven2/com/squareup/okio/okio-jvm/3.10.2/okio-jvm-3.10.2.jar",
        File(outputDirectory, "okio-jvm-3.10.2.jar"),
        "fd0a7e76c6731f00e920b7bc11c05d823a932045431add548e095de020a69ede",
        "SHA-256"
    ),
    DirectLibrary(
        "https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-serialization-core-jvm/1.8.0/kotlinx-serialization-core-jvm-1.8.0.jar",
        File(outputDirectory, "kotlinx-serialization-core-jvm-1.8.0.jar"),
        "d3c94e9d829bba6e0c4cd3ae478a40846dd49d5475d6707877be853976afe416",
        "SHA-256"
    ),
    DirectLibrary(
        "https://repo1.maven.org/maven2/com/charleskorn/kaml/kaml-jvm/0.72.0/kaml-jvm-0.72.0.jar",
        File(outputDirectory, "kaml-jvm-0.72.0.jar"),
        "fe8a76c4d866723f2a4f03f9028d4822750a95a882d27c3a77559cc04e3e73f2",
        "SHA-256"
    ),
) {

    fun download(): RequiredLights {
        for (library in this) {
            if (outputDirectory.walk().filter { it.isFile && it.extension == "jar" }.contains(library.outputFile)) {
                continue
            }
            library.download()
        }
        return this
    }

    fun load(classLoader: LightClassLoader): RequiredLights {
        for (library in this) {
            classLoader.load(library.outputFile)
        }
        return this
    }
}