package cloud.glitchdev.rfu.events.managers.inventory

import cloud.glitchdev.rfu.data.EquipmentSet
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

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

    class EquipmentChangeEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (EquipmentSet) -> Unit
    ) : ManagedTask<(EquipmentSet) -> Unit, EquipmentChangeEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
