package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.data.collections.CollectionItem
import cloud.glitchdev.rfu.events.AbstractEventManager

object CollectionEvents : AbstractEventManager<(item: CollectionItem, amount: Long, total: Long, isSync: Boolean) -> Unit, CollectionEvents.CollectionUpdateEvent>() {
    
    override val runTasks: (CollectionItem, Long, Long, Boolean) -> Unit = { item, amount, total, isSync ->
        safeExecution {
            tasks.sortedBy { it.priority }.forEach { task ->
                task.callback(item, amount, total, isSync)
            }
        }
    }

    fun registerCollectionUpdateEvent(priority: Int = 20, callback: (item: CollectionItem, amount: Long, total: Long, isSync: Boolean) -> Unit): CollectionUpdateEvent {
        return CollectionUpdateEvent(priority, callback).register()
    }

    class CollectionUpdateEvent(
        priority: Int = 20,
        callback: (item: CollectionItem, amount: Long, total: Long, isSync: Boolean) -> Unit
    ) : ManagedTask<(item: CollectionItem, amount: Long, total: Long, isSync: Boolean) -> Unit, CollectionUpdateEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
    
    fun trigger(item: CollectionItem, amount: Long, total: Long, isSync: Boolean) {
        runTasks(item, amount, total, isSync)
    }
}
