package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.AQUAMARINE
import cloud.glitchdev.rfu.constants.text.TextColor.GRAY
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_GREEN
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.events.managers.registerTickEvent
import cloud.glitchdev.rfu.feature.fishing.FrozenBlazeAura
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import kotlin.math.ceil
import kotlin.time.Duration.Companion.seconds

@HudElement
object FrozenBlazeDisplay : AbstractTextHudElement("frozenBlazeAura") {
    private var wasActive = false

    override val relatedSetting = "Frozen Blaze"
    override val requirement: Boolean
        get() = GeneralFishing.frozenBlazeDisplay
    override val isElementActive: Boolean
        get() = FrozenBlazeAura.hasFrozenBlazeArmor

    override fun onInitialize(scope: EventScope) {
        super.onInitialize(scope)

        scope.registerTickEvent(interval = 5) {
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

        // Preview
        if (isEditing && !FrozenBlazeAura.hasFrozenBlazeArmor) {
            text.setText("$label${LIGHT_GREEN}30s")
            return
        }

        val seconds = ceil(FrozenBlazeAura.remainingDuration / 1.seconds).toInt()
        val value = when {
            seconds <= 0 -> "${LIGHT_RED}${BOLD}INACTIVE ${GRAY}(Move!)"
            seconds <= 5 -> "${LIGHT_RED}${seconds}s"
            seconds <= 10 -> "${YELLOW}${seconds}s"
            else -> "${LIGHT_GREEN}${seconds}s"
        }
        text.setText("$label$value")
    }
}
