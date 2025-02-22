package org.sayandev.light.repository

import kotlinx.coroutines.Deferred
import org.sayandev.light.dependency.Dependency
import org.sayandev.light.AsyncDispatcher
import java.io.File

interface Repository {
    val name: String
    val uri: String

    fun download(directory: File, dispatcher: AsyncDispatcher, dependency: Dependency): Deferred<File>
}