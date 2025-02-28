package org.sayandev.light

import org.sayandev.LightClassLoader
import java.io.File

object KClassLoaderManager : LightClassLoader {
    val classLoader = ClassLoader.getSystemClassLoader()
    val method = classLoader::class.java
        .getDeclaredMethod("appendToClassPathForInstrumentation", String::class.java)
        .apply { isAccessible = true }

    override fun load(file: File) {
        method.invoke(classLoader, file.absolutePath)
    }
}