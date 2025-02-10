package org.sayandev.repository

interface Dependency {
    val group: String
    val artifact: String

    fun versions(repository: Repository): List<Version>
    fun lastVersion(repository: Repository): Version
    fun lastReleaseVersion(repository: Repository): Version

    fun metaURI(repository: Repository): String

    fun versionMetaURI(repository: Repository, version: Version): String

    fun downloadURI(repository: Repository, version: Version): String
}