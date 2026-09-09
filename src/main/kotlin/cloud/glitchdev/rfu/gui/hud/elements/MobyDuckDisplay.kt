package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.fishing.MobyDuckFeature
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.toReadableString

@HudElement
object MobyDuckDisplay : AbstractTextHudElement("mobyDuckDisplay") {
    override val requirement: Boolean
        get() = GeneralFishing.mobyDuckDisplay

    override val isElementActive: Boolean
        get() = MobyDuckFeature.isActive

    override fun onInitialize() {
        super.onInitialize()
        registerTickEvent(interval = 10) {
            if (isEditing || MobyDuckFeature.isActive) {
                updateState()
            }
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()

        if (isEditing && !MobyDuckFeature.isActive) {
            text.setText("${TextColor.PURPLE}${TextEffects.BOLD}Moby Duck ${TextColor.CYAN}+30☯${TextColor.WHITE}: 59m 59s")
            return
        }

        if (!MobyDuckFeature.isActive) {
            text.setText("")
            return
        }

        val duckColor = if (MobyDuckFeature.isCollectorsEdition) TextColor.PURPLE else TextColor.LIGHT_BLUE
        val wisdom = MobyDuckFeature.wisdomValue
        val remainingStr = MobyDuckFeature.remainingDuration.toReadableString()

        text.setText("$duckColor${TextEffects.BOLD}Moby Duck ${TextColor.CYAN}+${wisdom}☯${TextColor.WHITE}: $remainingStr")
    }
}
