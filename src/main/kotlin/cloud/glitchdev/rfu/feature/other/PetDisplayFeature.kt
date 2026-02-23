package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.SlotClickedEvents.registerSlotClickedEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.PetDisplay
import cloud.glitchdev.rfu.manager.other.OtherManager
import cloud.glitchdev.rfu.manager.other.data.StringEntry
import cloud.glitchdev.rfu.utils.dsl.removeFormatting
import gg.essential.universal.utils.toFormattedString
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

@RFUFeature
object PetDisplayFeature : Feature {
    val PETS_SCREEN_REGEX = """Pets \(\d+\/\d+\)""".toRegex()
    const val PET_REGEX = """(?:⭐ )?\[Lvl (\d+)] (?:(\[\d+✦\]) )?(.+)(?: ✦)?"""
    val AUTOPET_REGEX = """Autopet equipped your $PET_REGEX! VIEW RULE""".toRegex()
    val COLORED_AUTOPET_REGEX = """§r§cAutopet §r§eequipped your (.+)§r§e! §r§a§lVIEW RULE""".toRegex()
    val DESPAWNED_REGEX = """You despawned your .+!""".toRegex()
    const val SAVE_FIELD = "pet_display"

    override fun onInitialize() {
        val entry = OtherManager.getField(SAVE_FIELD) {
            StringEntry(null)
        } as? StringEntry ?: StringEntry(null)

        updateDisplay(entry.value)

        registerSlotClickedEvent { slot ->
            if(!OtherSettings.petDisplay) return@registerSlotClickedEvent
            val screen = mc.screen as? AbstractContainerScreen<*> ?: return@registerSlotClickedEvent
            if(!PETS_SCREEN_REGEX.matches(screen.title.string.trim())) return@registerSlotClickedEvent
            val pet = slot.item.customName?.toFormattedString() ?: return@registerSlotClickedEvent
            if(!PET_REGEX.toRegex().matches(pet.removeFormatting())) return@registerSlotClickedEvent
            updateDisplay(pet)
        }

        registerGameEvent(AUTOPET_REGEX) { text, _, _ ->
            val text = text.toFormattedString()
            val result = COLORED_AUTOPET_REGEX.find(text)?.groupValues ?: return@registerGameEvent
            updateDisplay(result.getOrNull(1))
        }

        registerGameEvent(DESPAWNED_REGEX) { _, _, _ ->
            updateDisplay(null)
        }
    }

    fun updateDisplay(string : String?) {
        OtherManager.setField(SAVE_FIELD, StringEntry(string))
        PetDisplay.currentPet = string
    }
}