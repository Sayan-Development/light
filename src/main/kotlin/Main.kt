package org.sayandev

import com.google.gson.JsonParser
import sun.misc.Unsafe
import java.io.File
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.net.URL
import java.net.URLClassLoader

object File {
    val fileURL = this::class.java.classLoader.getResource("gson-2.12.1.jar")
        ?: throw IllegalStateException("JAR not found in resources")
    val file = File(fileURL.file)
}

fun main() {
    val classLoader = ClassLoader.getSystemClassLoader()
    val method = classLoader::class.java.getDeclaredMethod("appendToClassPathForInstrumentation", String::class.java)
    method.isAccessible = true
    method.invoke(classLoader, org.sayandev.File.file.path)
    println(JsonParser.parseString("{\"foo\":\"bar\"}"))
}