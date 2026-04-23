package cloud.glitchdev.rfu.utils

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
}
