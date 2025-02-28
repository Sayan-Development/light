package org.sayandev;

import org.sayandev.light.MainKt;

import java.io.File;

public class JavaTest {
    public static void main(String[] args) {
        KotlinLight.shine(new ClassLoaderManager(), new File("./downloads"));
        MainKt.main();
    }
}
