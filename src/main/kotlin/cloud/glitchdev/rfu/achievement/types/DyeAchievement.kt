package cloud.glitchdev.rfu.achievement.types

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.registerDyeDropEvent
import cloud.glitchdev.rfu.utils.Coroutines
import gg.essential.universal.utils.toUnformattedString
import kotlinx.coroutines.Job
import net.minecraft.core.component.DataComponents

abstract class DyeAchievement(val dye: Dyes) : BaseAchievement() {
    private val DROPPED_REGEX = """You've dropped: (\d+)""".toRegex()
    private val DYE_GUIDE_REGEX = """\(\d+/\d+\) Dye Guide""".toRegex()

    override fun setupListeners(scope: EventScope) {
        var pendingCompletion: Job? = null
        scope.onClose {
            pendingCompletion?.cancel()
            pendingCompletion = null
        }

        scope.registerDyeDropEvent { dyeDrop, _, _ ->
            if (dyeDrop == dye && pendingCompletion == null) {
                pendingCompletion = Coroutines.setTimeout(100) {
                    mc.execute {
                        if (pendingCompletion != null) complete()
                    }
                }
            }
        }

        scope.registerContainerOpenEvent { containerName, _, items ->
            if (DYE_GUIDE_REGEX.matches(containerName)) {
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
        }
    }
}
