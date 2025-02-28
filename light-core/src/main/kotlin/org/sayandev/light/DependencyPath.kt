package org.sayandev.light

import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

@Serializable
data class DependencyPath(
    val path: String
) {
    val checksum = getChecksum()

    fun file(relativeDirectory: File): File {
        return File(relativeDirectory, path)
    }

    fun file(): File {
        return File(path)
    }

    fun getChecksum(algorithm: String = "SHA-256"): String {
        val digest = MessageDigest.getInstance(algorithm)
        FileInputStream(file()).use { fis ->
            val byteArray = ByteArray(1024)
            var bytesCount: Int
            while (fis.read(byteArray).also { bytesCount = it } != -1) {
                digest.update(byteArray, 0, bytesCount)
            }
        }
        val bytes = digest.digest()
        val sb = StringBuilder()
        for (byte in bytes) {
            sb.append(String.format("%02x", byte))
        }
        return sb.toString()
    }
}