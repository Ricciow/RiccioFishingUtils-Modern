package cloud.glitchdev.rfu.constants.ui

import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.XConstraint
import gg.essential.elementa.dsl.pixels

enum class TextAlignment(val displayName: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right");

    fun toXConstraint(): XConstraint = when (this) {
        LEFT -> 0.pixels()
        CENTER -> CenterConstraint()
        RIGHT -> 0.pixels(alignOpposite = true)
    }

    override fun toString(): String = displayName
}