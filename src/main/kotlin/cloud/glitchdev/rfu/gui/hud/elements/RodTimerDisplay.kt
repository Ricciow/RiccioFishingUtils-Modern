package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.width

@HudElement
object RodTimerDisplay : AbstractTextHudElement("rodTimer") {
    const val DEFAULT_MAX_TIME = 3f
    private const val PREVIEW_TIME = 1.5f

    var rodTime : Float = -1f
    var maxTime : Float = DEFAULT_MAX_TIME
    override val relatedSetting = "Rod Timer"
    override val requirement: Boolean
        get() = GeneralFishing.rodTimerDisplay
    override val isElementActive: Boolean
        get() = rodTime >= 0

    override fun onUpdateState() {
        super.onUpdateState()

        val format = GeneralFishing.rodTimerStyle.format
        val string = if (rodTime == 0f) {
            format.ready()
        } else {
            val time = if (rodTime > 0) rodTime else PREVIEW_TIME
            format.countdown(time, remainingFraction(time))
        }

        val widest = listOf(
            format.ready(),
            format.countdown(DEFAULT_MAX_TIME, 1f),
            format.countdown(0.1f, 0f),
            string
        ).flatMap { it.lines() }.maxOf { it.width(scale) }

        container.constrain {
            width = widest.pixels()
        }

        text.constrain {
            x = format.alignment.toXConstraint()
        }

        text.setText(string, format.alignment)
    }

    private fun remainingFraction(time: Float): Float {
        return ((time - 0.1f) / (maxTime - 0.2f)).coerceIn(0f, 1f)
    }
}
