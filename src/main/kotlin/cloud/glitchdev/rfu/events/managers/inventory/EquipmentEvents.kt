package cloud.glitchdev.rfu.events.managers.inventory

import cloud.glitchdev.rfu.data.EquipmentSet
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY

object EquipmentEvents {
    val currentEquipmentSet: EquipmentSet
        get() = EquipmentChangeEventManager.currentEquipmentSet

    fun registerEquipmentChangeEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (equipmentSet: EquipmentSet) -> Unit
    ): EquipmentChangeEventManager.EquipmentChangeEvent {
        return EquipmentChangeEventManager.register(priority, callback)
    }
}
