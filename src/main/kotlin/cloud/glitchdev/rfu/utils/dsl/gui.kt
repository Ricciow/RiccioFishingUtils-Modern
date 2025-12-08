package cloud.glitchdev.rfu.utils.dsl

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.constraints.PixelConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SuperConstraint
import gg.essential.elementa.constraints.animation.AnimationStrategy
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate

fun <T : UIComponent> T.setHidden(state: Boolean) : UIComponent {
    if(state) {
        this.hide()
    }
    else {
        this.unhide()
    }

    return this
}

fun <T : UIComponent> T.addHoverColoring(strategy: AnimationStrategy, duration : Float, primaryColor : ColorConstraint, hoverColor : ColorConstraint) : UIComponent {
    this.onMouseEnter {
        this.animate {
            setColorAnimation(strategy, duration, hoverColor)
        }
    }.onMouseLeave {
        this.animate {
            setColorAnimation(strategy, duration, primaryColor)
        }
    }

    return this
}

fun PixelConstraint.changeTarget(target: UIComponent) : PixelConstraint {
    this.constrainTo = target
    return this
}

fun RelativeConstraint.changeTarget(target: UIComponent) : RelativeConstraint {
    this.constrainTo = target
    return this
}

fun CenterConstraint.changeTarget(target: UIComponent) : CenterConstraint {
    this.constrainTo = target
    return this
}

fun ChildBasedSizeConstraint.changeTarget(target: UIComponent) : ChildBasedSizeConstraint {
    this.constrainTo = target
    return this
}