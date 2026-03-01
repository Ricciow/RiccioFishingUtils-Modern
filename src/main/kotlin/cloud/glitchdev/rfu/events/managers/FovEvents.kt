package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.wrappers.Cancelable

object FovEvents : AbstractEventManager<(Cancelable<Float>) -> Unit, FovEvents.FovEvent>() {
    fun runTasks(cancelable : Cancelable<Float>) {
        for (task in tasks) {
            task.callback(cancelable)
        }
    }

    fun registerFovEvent(priority: Int = 20, callback: (Cancelable<Float>) -> Unit): FovEvent {
        return FovEvent(priority, callback).register()
    }

    class FovEvent(
        priority: Int,
        callback: (Cancelable<Float>) -> Unit
    ) : ManagedTask<(Cancelable<Float>) -> Unit, FovEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}