package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import java.awt.Color

class MultilineHudText(private var scale: Float) : UIContainer() {
    data class HudLine(val text: String, val color: Color = Color.WHITE)

    private var lines: List<UIText> = emptyList()
    private var currentText: String = ""

    init {
        constrain {
            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        }
    }

    fun setLines(hudLines: List<HudLine>) {
        currentText = hudLines.joinToString("\n") { it.text }
        lines.forEach { removeChild(it) }
        lines = hudLines.map { line ->
            UIText(line.text).constrain {
                x = 0.pixels()
                y = SiblingConstraint()
                width = ScaledTextConstraint(scale)
                height = TextAspectConstraint()
                this.color = line.color.toConstraint()
            } childOf this
        }
    }

    fun setText(value: String) {
        if (value.isEmpty()) {
            setLines(emptyList())
        } else {
            setLines(value.split("\n").map { HudLine(it) })
        }
    }

    fun getText(): String = currentText

    fun updateScale(newScale: Float) {
        scale = newScale
        lines.forEach { line ->
            line.constrain {
                width = ScaledTextConstraint(scale)
            }
        }
    }
}
