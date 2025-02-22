package org.sayandev.light

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class AsyncDispatcher(
    threadPrefix: String,
    threads: Int
) : CoroutineDispatcher() {

    private val threadPool: ExecutorService = Executors.newFixedThreadPool(
        threads.coerceAtLeast(1)
    ) { runnable ->
        Thread(runnable).apply {
            name = "$threadPrefix-${this.id}"
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        threadPool.submit(block)
    }
}