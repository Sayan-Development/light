package org.sayandev

import kotlinx.coroutines.*

object CoroutineUtils {
    suspend fun <T> Deferred<T>.awaitWithTimeout(timeout: Long, onTimeout: (TimeoutCancellationException) -> Unit = {}): T? {
        return try {
            withTimeout(timeout) {
                await()
            }
        } catch (e: TimeoutCancellationException) {
            onTimeout(e)
            null
        }
    }

    fun launch(
        dispatcher: CoroutineDispatcher,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val session = CoroutineScope(dispatcher)
        if (!session.isActive) {
            return Job()
        }

        return session.launch(dispatcher, start, block)
    }
}