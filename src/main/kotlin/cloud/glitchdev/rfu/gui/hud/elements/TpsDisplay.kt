package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.other.TpsTracker
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import java.util.Locale

@HudElement
object TpsDisplay : AbstractTextHudElement("tpsDisplay") {
    override val requirement: Boolean
        get() = OtherSettings.tpsDisplay

    override fun onInitialize() {
        super.onInitialize()
        registerTickEvent(interval = 10) {
            if (isEditing || requirement) {
                updateState()
            }
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()

        if (isEditing && !requirement) {
            text.setText("${TextColor.WHITE}TPS: ${TextColor.LIGHT_GREEN}20.0")
            return
        }

        val tps = TpsTracker.getTps()
        val color = when {
            tps >= 19.5 -> TextColor.LIGHT_GREEN
            tps >= 17.0 -> TextColor.YELLOW
            tps >= 12.0 -> TextColor.GOLD
            else -> TextColor.LIGHT_RED
        }

        text.setText("${TextColor.WHITE}TPS: $color${"%.1f".format(Locale.ROOT, tps)}")
    }
}
