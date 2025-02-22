package org.sayandev.light

import java.io.File

object KClassLoaderManager {
    val classLoader = ClassLoader.getSystemClassLoader()
    val method = classLoader::class.java
        .getDeclaredMethod("appendToClassPathForInstrumentation", String::class.java)
        .apply { isAccessible = true }

    fun load(file: File) {
        method.invoke(classLoader, file.absolutePath)
    }
}