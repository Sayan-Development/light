package org.sayandev.download

import org.sayandev.repository.Dependency
import java.io.InputStream

data class FetchData(
    val dependency: Dependency,
    val uri: String,
    val inputStream: InputStream
)