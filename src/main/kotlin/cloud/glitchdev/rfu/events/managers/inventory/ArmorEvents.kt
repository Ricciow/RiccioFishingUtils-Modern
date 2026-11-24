package cloud.glitchdev.rfu.events.managers.inventory

import cloud.glitchdev.rfu.data.ArmorSet
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY

object ArmorEvents {
    val currentArmorSet: ArmorSet
        get() = ArmorChangeEventManager.currentArmorSet

    fun registerArmorChangeEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (armorSet: ArmorSet) -> Unit
    ): ArmorChangeEventManager.ArmorChangeEvent {
        return ArmorChangeEventManager.register(priority, callback)
    }
}
