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

class MultilineHudText(private var scale: Float) : UIContainer() {
    private var lines: List<UIText> = emptyList()
    private var currentText: String = ""

    init {
        constrain {
            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        }
    }

    fun setText(value: String) {
        currentText = value
        lines.forEach { removeChild(it) }
        lines = value.split("\n").map { line ->
            UIText(line).constrain {
                x = 0.pixels()
                y = SiblingConstraint()
                width = ScaledTextConstraint(scale)
                height = TextAspectConstraint()
            } childOf this
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
