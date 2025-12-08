package cloud.glitchdev.rfu.utils.dsl

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.constraints.animation.AnimationStrategy
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.font.DefaultFonts
import gg.essential.elementa.font.FontProvider

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

fun String.height(textScale: Float = 1f, fontProvider: FontProvider = DefaultFonts.VANILLA_FONT_RENDERER) =
    fontProvider.getStringHeight(this, 10f) * textScale