package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.data.ArmorSet
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.SetSlotEvents.registerSetSlotEvent
import cloud.glitchdev.rfu.utils.Coroutines
import kotlinx.coroutines.Job

object ArmorEvents {
    val currentArmorSet: ArmorSet
        get() = ArmorChangeEventManager.currentArmorSet

    fun registerArmorChangeEvent(
        priority: Int = 20,
        callback: (armorSet: ArmorSet) -> Unit
    ): ArmorChangeEventManager.ArmorChangeEvent {
        return ArmorChangeEventManager.register(priority, callback)
    }

    @AutoRegister
    object ArmorChangeEventManager : AbstractEventManager<(ArmorSet) -> Unit, ArmorChangeEventManager.ArmorChangeEvent>(), RegisteredEvent {
        var currentArmorSet: ArmorSet = ArmorSet()
            private set

        private var debounceJob: Job? = null
        private const val DEBOUNCE_DELAY_MS = 100L

        override val runTasks: (ArmorSet) -> Unit = { armorSet ->
            safeExecution {
                tasks.forEach { task -> task.callback(armorSet) }
            }
        }

        override fun register() {
            registerSetSlotEvent { containerId, slot, _ ->
                if (debounceJob != null || !isArmorSlot(containerId, slot)) return@registerSetSlotEvent

                debounceJob = Coroutines.setTimeout(DEBOUNCE_DELAY_MS) {
                    try {
                        checkArmorUpdate()
                    } finally {
                        debounceJob = null
                    }
                }
            }
        }

        private fun isArmorSlot(containerId: Int, slot: Int): Boolean {
            return (containerId == 0 && slot in 5..8) || (containerId == -2 && slot in 36..39)
        }

        private fun checkArmorUpdate() {
            val player = mc.player ?: return
            val newSet = ArmorSet.fromPlayer(player)

            if (currentArmorSet.hasChanged(newSet)) {
                currentArmorSet = newSet
                runTasks(newSet)
            }
        }

        fun register(
            priority: Int = 20,
            callback: (ArmorSet) -> Unit
        ): ArmorChangeEvent {
            return ArmorChangeEvent(priority, callback).register()
        }

        class ArmorChangeEvent(
            priority: Int = 20,
            callback: (ArmorSet) -> Unit
        ) : ManagedTask<(ArmorSet) -> Unit, ArmorChangeEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
