package org.sayandev.repository

import java.io.File

interface Repository {
    val url: String

    fun download(dependency: Dependency, directory: File)
}