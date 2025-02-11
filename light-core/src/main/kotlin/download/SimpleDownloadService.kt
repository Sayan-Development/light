package org.sayandev.download

import org.sayandev.URLConnection
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class SimpleDownloadService : DownloadService {
    fun fetch(uri: String, fallbackURI: String?): InputStream? {
        val connection = let {
            val connection = URLConnection(uri).open()
            if (connection.responseCode != 200 && fallbackURI != null) {
                connection.disconnect()
                val fallbackConnection = URLConnection(fallbackURI).open()
                if (fallbackConnection.responseCode != 200) {
                    fallbackConnection.disconnect()
                    null
                } else fallbackConnection
            } else connection
        }

        return if (connection != null) {
            connection.connect()
            val buffer = ByteArrayOutputStream()
            connection.inputStream.use { it.copyTo(buffer) }
            val byteArrayInputStream = ByteArrayInputStream(buffer.toByteArray())
            connection.disconnect()
            byteArrayInputStream
        } else {
            null
        }
    }

    fun download(inputStream: InputStream, file: File): File {
        file.outputStream().use { inputStream.copyTo(it) }
        return file
    }
}