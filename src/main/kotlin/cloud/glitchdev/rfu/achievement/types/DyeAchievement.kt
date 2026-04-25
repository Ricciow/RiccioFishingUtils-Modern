package cloud.glitchdev.rfu.achievement.types

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.constants.Dyes
import cloud.glitchdev.rfu.events.managers.ContainerEvents
import cloud.glitchdev.rfu.events.managers.DropEvents
import cloud.glitchdev.rfu.utils.Coroutines
import gg.essential.universal.utils.toUnformattedString
import kotlinx.coroutines.delay
import net.minecraft.core.component.DataComponents

abstract class DyeAchievement(val dye: Dyes) : BaseAchievement() {
    private val DROPPED_REGEX = """You've dropped: (\d+)""".toRegex()

    override fun setupListeners() {
        activeListeners.add(DropEvents.registerDyeDropEvent { dyeDrop, _ ->
            if (dyeDrop == dye) {
                Coroutines.launch {
                    delay(100)
                    complete()
                }
            }
        })

        activeListeners.add(ContainerEvents.registerContainerOpenEvent { _, items ->
            if (mc.screen?.title?.string == "Dye Compendium") {
                val item = items.find { it.hoverName.toUnformattedString() == dye.dyeName }
                    ?: return@registerContainerOpenEvent
                val lore = item[DataComponents.LORE] ?: return@registerContainerOpenEvent

                for (line in lore.lines) {
                    val plainText = line.toUnformattedString()
                    val match = DROPPED_REGEX.find(plainText)
                    if (match != null) {
                        val count = match.groupValues[1].toIntOrNull() ?: 0
                        if (count > 0) complete()
                        break
                    }
                }
            }
        })
    }
}