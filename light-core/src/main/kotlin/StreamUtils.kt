package org.sayandev

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

object StreamUtils {
    fun InputStream.output(): String {
        val outputStream = ByteArrayOutputStream()
        this.copyTo(outputStream)
        return outputStream.toString(StandardCharsets.UTF_8.name())
    }
}