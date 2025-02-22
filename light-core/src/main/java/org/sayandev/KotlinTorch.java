package org.sayandev;

import org.sayandev.light.MainKt;

import java.io.File;

public class KotlinTorch {
    public static void shine(File libraryDirectory) {
        BaseLibrary kotlin = new BaseLibrary("https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/2.1.0/kotlin-stdlib-2.1.0.jar", new File(libraryDirectory, "kotlin-stdlib-2.1.0.jar"));
        kotlin.download();
        ClassLoaderManager.load(kotlin.getOutputFile());

        MainKt.main();
    }
}
