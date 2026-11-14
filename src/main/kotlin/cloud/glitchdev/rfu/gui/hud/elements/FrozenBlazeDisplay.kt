package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.AQUAMARINE
import cloud.glitchdev.rfu.constants.text.TextColor.GRAY
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_GREEN
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.fishing.FrozenBlazeAura
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import kotlin.math.ceil

@HudElement
object FrozenBlazeDisplay : AbstractTextHudElement("frozenBlazeAura") {
    private var wasActive = false

    override val relatedSetting = "Frozen Blaze"
    override val requirement: Boolean
        get() = GeneralFishing.frozenBlazeDisplay
    override val isElementActive: Boolean
        get() = FrozenBlazeAura.hasFrozenBlazeArmor

    override fun onInitialize() {
        super.onInitialize()

        registerTickEvent(interval = 5) {
            val active = isElementActive
            if (isEditing || active || wasActive) {
                updateState()
            }
            wasActive = active
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()

        val label = "$AQUAMARINE${BOLD}Frozen Blaze: "

        // Preview while positioning the element without the armor on.
        if (isEditing && !FrozenBlazeAura.hasFrozenBlazeArmor) {
            text.setText("$label${LIGHT_GREEN}30s")
            return
        }

        val seconds = ceil(FrozenBlazeAura.remainingMs / 1000.0).toInt()
        val value = when {
            seconds <= 0 -> "${LIGHT_RED}${BOLD}INACTIVE ${GRAY}(move!)"
            seconds <= 5 -> "${LIGHT_RED}${seconds}s"
            seconds <= 10 -> "${YELLOW}${seconds}s"
            else -> "${LIGHT_GREEN}${seconds}s"
        }
        text.setText("$label$value")
    }
}
