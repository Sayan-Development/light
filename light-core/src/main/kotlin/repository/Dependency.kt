package org.sayandev.repository

import org.sayandev.DependencyManager
import java.io.File

interface Dependency {
    var file: File?
    val group: String
    val artifact: String
    val version: Version

    fun download(manager: DependencyManager, includeTransitives: Boolean, async: Boolean): File

    fun versionMetaURI(repository: Repository, version: Version): String

    fun downloadURI(repository: Repository, version: Version): String

    fun jvmDownloadURI(repository: Repository, version: Version): String
}