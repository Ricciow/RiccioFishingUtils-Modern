package cloud.glitchdev.rfu.utils.gui

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.Window
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

fun <T : UIComponent> T.isHidden() : Boolean {
    return !this.hasParent || !this.parent.children.contains(this)
}

fun <T : UIComponent> T.isDeepHidden(): Boolean {
    var current: UIComponent? = this
    while (current != null) {
        if (current is Window) return false
        if (!current.hasParent) return true
        val parent = current.parent
        if (parent === current) return false
        if (!parent.children.contains(current)) return true
        current = parent
    }
    return true
}

fun <T : UIComponent> T.toggleHidden() : UIComponent {
    if(this.parent.children.contains(this)) {
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

fun String.width(textScale: Float = 1f, fontProvider: FontProvider = DefaultFonts.VANILLA_FONT_RENDERER) =
    fontProvider.getStringWidth(this, 10f) * textScale
