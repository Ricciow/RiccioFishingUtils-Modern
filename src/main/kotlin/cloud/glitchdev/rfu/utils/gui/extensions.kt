package cloud.glitchdev.rfu.utils.gui

import gg.essential.elementa.UIComponent
import gg.essential.elementa.font.DefaultFonts
import gg.essential.elementa.font.FontProvider

fun <T : UIComponent> T.setHidden(state: Boolean) : UIComponent {
    val currentlyHidden = isHidden()
    if (state) {
        if (!currentlyHidden) {
            this.hide(true)
        }
    } else {
        if (currentlyHidden) {
            this.unhide()
        }
    }
    return this
}

fun <T : UIComponent> T.isHidden() : Boolean {
    return !this.hasParent || !this.parent.children.contains(this)
}

fun String.height(textScale: Float = 1f, fontProvider: FontProvider = DefaultFonts.VANILLA_FONT_RENDERER) =
    fontProvider.getStringHeight(this, 10f) * textScale

fun String.width(textScale: Float = 1f, fontProvider: FontProvider = DefaultFonts.VANILLA_FONT_RENDERER) =
    fontProvider.getStringWidth(this, 10f) * textScale
