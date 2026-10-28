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
    override val relatedSetting = "TPS Display"
    override val requirement: Boolean
        get() = OtherSettings.tpsDisplay
    override val isElementActive: Boolean
        get() = TpsTracker.getTps() != null

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

        val tps = TpsTracker.getTps() ?: if (isEditing) 20.0 else {
            text.clearLines()
            return
        }

        val color = when {
            tps >= 19.5 -> TextColor.LIGHT_GREEN
            tps >= 17.0 -> TextColor.YELLOW
            tps >= 12.0 -> TextColor.GOLD
            else -> TextColor.LIGHT_RED
        }

        text.setText("${TextColor.WHITE}TPS: $color${"%.1f".format(Locale.ROOT, tps)}")
    }
}
