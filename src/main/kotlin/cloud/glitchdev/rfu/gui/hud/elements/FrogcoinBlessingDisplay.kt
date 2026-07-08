package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.LotusAtollSettings
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import cloud.glitchdev.rfu.feature.fishing.FrogcoinBlessingFeature
import kotlin.time.Duration.Companion.milliseconds

@HudElement
object FrogcoinBlessingDisplay : AbstractTextHudElement("frogcoinBlessing") {

    override val enabled: Boolean
        get() = LotusAtollSettings.frogcoinBlessingDisplay && (isEditing || (World.island == FishingIslands.ATOLL && FrogcoinBlessingFeature.activeBlessings.isNotEmpty()))

    override fun onInitialize() {
        super.onInitialize()

        registerTickEvent(interval = 20) {
            updateState()
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()
        val activeBlessings = FrogcoinBlessingFeature.activeBlessings

        if (isEditing && activeBlessings.isEmpty()) {
            val preview = buildString {
                append("${TextColor.DARK_GREEN}${TextEffects.BOLD}Blessings:\n")
                append("${TextColor.CYAN}+2.5\uE021 Sea Creature Chance: ${TextColor.WHITE}29m 59s\n")
                append("${TextColor.GOLD}+5\uE02A Trophy Chance: ${TextColor.WHITE}29m 59s")
            }
            text.setText(preview)
            return
        }

        if (activeBlessings.isEmpty()) {
            text.setText("")
            return
        }

        val now = System.currentTimeMillis()
        val lines = mutableListOf<String>()
        lines.add("${TextColor.DARK_GREEN}${TextEffects.BOLD}Blessings:")

        for ((buff, expireTime) in activeBlessings) {
            val remainingMillis = expireTime - now
            if (remainingMillis > 0) {
                val duration = remainingMillis.milliseconds
                val buffColor = when {
                    buff.contains("Sea Creature") -> TextColor.CYAN
                    buff.contains("Trophy") -> TextColor.GOLD
                    buff.contains("Treasure") -> TextColor.YELLOW
                    buff.contains("Double Hook") -> TextColor.LIGHT_BLUE
                    else -> TextColor.WHITE
                }
                lines.add("$buffColor+$buff: ${TextColor.WHITE}${duration.toReadableString()}")
            }
        }

        text.setText(lines.joinToString("\n"))
    }
}
