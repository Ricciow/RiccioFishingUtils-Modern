package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.utils.gui.Gui
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.toConstraint

object DeadWindow : WindowScreen(ElementaVersion.V10, drawDefaultBackground = false) {
    val transparent = UIScheme.transparent.toConstraint()
    val bgColor = UIScheme.darkBackground.toConstraint()
    val textColor = UIScheme.diedColor.toConstraint()

    val bg = UIBlock().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = 100.percent()
        height = 25.percent()
        color = transparent
    } childOf window

    val text = UIText("You died.").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ScaledTextConstraint(4f)
        height = TextAspectConstraint()
        color = transparent
    } childOf bg

    fun open() {
        Gui.openGui(this)

        bg.animate {
            setColorAnimation(Animations.LINEAR, 2f, bgColor)
        }

        text.animate {
            setColorAnimation(Animations.LINEAR, 2f, textColor)
        }
    }
}