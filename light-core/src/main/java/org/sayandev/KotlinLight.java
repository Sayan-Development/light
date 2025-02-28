package org.sayandev;

import java.io.File;

public class KotlinLight {
    public static void shine(LightClassLoader classLoader, File libraryDirectory) {
        BaseLibrary kotlin = new BaseLibrary(
                "https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/2.1.0/kotlin-stdlib-2.1.0.jar",
                new File(libraryDirectory, "kotlin-stdlib-2.1.0.jar"),
                "d3028429e7151d7a7c1a0d63a4f60eac86a87b91",
                "SHA-1"
        );
        kotlin.download();
        classLoader.load(kotlin.getOutputFile());
    }
}
