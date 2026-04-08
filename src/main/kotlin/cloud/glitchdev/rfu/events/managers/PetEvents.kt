package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.StringEntry
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.SlotClickedEvents.registerSlotClickedEvent
import cloud.glitchdev.rfu.utils.dsl.removeFormatting
import gg.essential.universal.utils.toFormattedString
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

object PetEvents {
    fun registerPetUpdateEvent(
        priority: Int = 20,
        callback: (String?) -> Unit
    ): PetUpdateEventManager.PetUpdateEvent {
        return PetUpdateEventManager.register(priority, callback)
    }

    @AutoRegister
    object PetUpdateEventManager : AbstractEventManager<(String?) -> Unit, PetUpdateEventManager.PetUpdateEvent>(), RegisteredEvent {
        const val SAVE_FIELD = "current_pet"
        val PETS_SCREEN_REGEX = """Pets:?( ".+")?( \(\d+\/\d+\))?""".toRegex()
        const val PET_REGEX = """(?:⭐ )?\[Lvl (\d+)] (?:(\[\d+✦\]) )?(.+)(?: ✦)?"""
        val AUTOPET_REGEX = """Autopet equipped your $PET_REGEX! VIEW RULE""".toRegex()
        val COLORED_AUTOPET_REGEX = """§r§cAutopet §r§eequipped your (.+)§r§e! §r§a§lVIEW RULE""".toRegex()
        val DESPAWNED_REGEX = """You despawned your .+!""".toRegex()
        val LEVEL_UP_REGEX = """Your (.+) leveled up to level (\d+)!""".toRegex()
        val LEVEL_IN_PET_REGEX = """\[Lvl \d+]""".toRegex()

        var currentPet: String? = null
            private set

        val currentPetName: String?
            get() = currentPet?.removeFormatting()?.let { PET_REGEX.toRegex().find(it)?.groupValues?.getOrNull(3) }

        override val runTasks: (String?) -> Unit = { pet ->
            safeExecution {
                tasks.forEach { task -> task.callback(pet) }
            }
        }

        override fun register() {
            val entry = OtherManager.getField(SAVE_FIELD) {
                StringEntry(null)
            } as? StringEntry ?: StringEntry(null)

            currentPet = entry.value

            registerSlotClickedEvent { slot ->
                val screen = mc.screen as? AbstractContainerScreen<*> ?: return@registerSlotClickedEvent
                if(!PETS_SCREEN_REGEX.matches(screen.title.string.trim())) return@registerSlotClickedEvent
                val pet = slot.item.hoverName.toFormattedString()
                if(!PET_REGEX.toRegex().matches(pet.removeFormatting())) return@registerSlotClickedEvent
                updatePet(pet)
            }

            registerGameEvent(AUTOPET_REGEX) { text, _, _ ->
                val text = text.toFormattedString()
                val result = COLORED_AUTOPET_REGEX.find(text)?.groupValues ?: return@registerGameEvent
                updatePet(result.getOrNull(1))
            }

            registerGameEvent(DESPAWNED_REGEX) { _, _, _ ->
                updatePet(null)
            }

            registerGameEvent(LEVEL_UP_REGEX) { text, _, _ ->
                val match = LEVEL_UP_REGEX.find(text.string) ?: return@registerGameEvent
                val leveledPetName = match.groupValues[1]
                val newLevel = match.groupValues[2]

                val current = currentPet ?: return@registerGameEvent
                val currentName = currentPetName ?: return@registerGameEvent

                if (currentName != leveledPetName) return@registerGameEvent

                updatePet(LEVEL_IN_PET_REGEX.replace(current, "[Lvl $newLevel]"))
            }
        }

        private fun updatePet(pet: String?) {
            currentPet = pet
            OtherManager.setField(SAVE_FIELD, StringEntry(pet))
            runTasks(pet)
        }

        fun register(priority: Int = 20, callback: (String?) -> Unit): PetUpdateEvent {
            return PetUpdateEvent(priority, callback).register()
        }

        class PetUpdateEvent(
            priority: Int = 20,
            callback: (String?) -> Unit
        ) : ManagedTask<(String?) -> Unit, PetUpdateEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
