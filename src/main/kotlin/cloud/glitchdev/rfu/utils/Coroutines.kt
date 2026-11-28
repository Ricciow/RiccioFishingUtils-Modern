package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.events.EventScope
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object Coroutines {
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return scope.launch(context, start, block)
    }

    fun launch(
        eventScope: EventScope,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val job = scope.launch(context, start, block)
        val cancelJob: () -> Unit = { job.cancel() }
        if (eventScope.addCleanupIfOpen(cancelJob)) {
            job.invokeOnCompletion { eventScope.removeCleanup(cancelJob) }
        } else {
            job.cancel()
        }
        return job
    }

    fun setTimeout(timeout: Long, callback: () -> Unit): Job {
        return launch {
            delay(timeout)
            callback()
        }
    }

    fun setTimeout(eventScope: EventScope, timeout: Long, callback: () -> Unit): Job {
        return launch(eventScope) {
            delay(timeout)
            callback()
        }
    }
}
