package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.data.EquipmentSet
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.EventManager

object EquipmentEvents {
    val currentEquipmentSet: EquipmentSet
        get() = EquipmentChangeEventManager.currentEquipmentSet

    fun registerEquipmentChangeEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (equipmentSet: EquipmentSet) -> Unit
    ): EquipmentChangeEventManager.EquipmentChangeEvent {
        return EquipmentChangeEventManager.register(priority, callback)
    }

    @EventManager(register = false)
    object EquipmentChangeEventManager : AbstractEventManager<(EquipmentSet) -> Unit, EquipmentChangeEventManager.EquipmentChangeEvent>() {
        var currentEquipmentSet: EquipmentSet = EquipmentSet()
            private set

        override val runTasks: (EquipmentSet) -> Unit = { equipmentSet ->
            safeExecution {
                tasks.forEach { task -> task.callback(equipmentSet) }
            }
        }

        fun updateEquipmentSet(newSet: EquipmentSet, forceNotify: Boolean = false) {
            if (forceNotify || currentEquipmentSet.hasChanged(newSet)) {
                currentEquipmentSet = newSet
                runTasks(newSet)
            }
        }

        fun register(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (EquipmentSet) -> Unit
        ): EquipmentChangeEvent {
            return EquipmentChangeEvent(priority, callback).register()
        }

        class EquipmentChangeEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (EquipmentSet) -> Unit
        ) : ManagedTask<(EquipmentSet) -> Unit, EquipmentChangeEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
