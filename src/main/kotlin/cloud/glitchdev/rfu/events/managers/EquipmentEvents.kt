package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.data.EquipmentSet
import cloud.glitchdev.rfu.events.AbstractEventManager

object EquipmentEvents {
    val currentEquipmentSet: EquipmentSet
        get() = EquipmentChangeEventManager.currentEquipmentSet

    fun registerEquipmentChangeEvent(
        priority: Int = 20,
        callback: (equipmentSet: EquipmentSet) -> Unit
    ): EquipmentChangeEventManager.EquipmentChangeEvent {
        return EquipmentChangeEventManager.register(priority, callback)
    }

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
            priority: Int = 20,
            callback: (EquipmentSet) -> Unit
        ): EquipmentChangeEvent {
            return EquipmentChangeEvent(priority, callback).register()
        }

        class EquipmentChangeEvent(
            priority: Int = 20,
            callback: (EquipmentSet) -> Unit
        ) : ManagedTask<(EquipmentSet) -> Unit, EquipmentChangeEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
