package org.sayandev;

import java.io.File;
import java.lang.reflect.Method;

class ClassLoaderManager {

    private static final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private static Method method;

    public ClassLoaderManager() {
        try {
            method = classLoader.getClass().getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
            method.setAccessible(true);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public static void load(File file) {
        try {
            method.invoke(classLoader, file.getAbsolutePath());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }
}
