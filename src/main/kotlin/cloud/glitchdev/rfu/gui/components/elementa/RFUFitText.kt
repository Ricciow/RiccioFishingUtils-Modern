package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.VanillaFontRenderer
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.basicHeightConstraint
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.width
import gg.essential.elementa.font.extractMcScale
import gg.essential.elementa.renderer.ElementaExtractor
import net.minecraft.client.Minecraft
import net.minecraft.locale.Language
import net.minecraft.network.chat.FormattedText
import java.awt.Color

/** Draws a single line at its requested scale, shrinking only when its rendered text exceeds the width. */
class RFUFitText(private var text: String, private val maxScale: Float = 1f) : UIComponent() {
    init {
        constrain {
            width = 100.percent
            textScale = maxScale.pixels
            height = basicHeightConstraint {
                val font = getFontProvider()
                val topPadding = if (constraints.y is CenterConstraint) font.getBelowLineHeight() else 0f
                (topPadding + font.getBaseLineHeight() + font.getBelowLineHeight() + font.getShadowHeight()) * getTextScale()
            }
        }
    }

    fun setText(text: String) {
        this.text = text
    }

    override fun getTextScale(): Float = constraints.textScale.getHeightImpl(this)

    fun getTextWidth(text: String = this.text): Float {
        if (getFontProvider() !is VanillaFontRenderer) return text.width(1f, getFontProvider())
        return Minecraft.getInstance().font.width(Language.getInstance().getVisualOrder(FormattedText.of(text))).toFloat()
    }

    override fun extractComponent(extractor: ElementaExtractor) {
        val naturalWidth = getTextWidth()
        val scale = if (naturalWidth <= 0f) getTextScale()
            else minOf(getTextScale(), getWidth().coerceAtLeast(0f) / naturalWidth)
        if (scale <= 0f) return
        val font = getFontProvider()
        val y = getTop() + if (constraints.y is CenterConstraint) font.getBelowLineHeight() * scale else 0f
        font.extractMcScale(
            extractor, text, getColor(), getLeft(), y, scale, true, Color.BLACK
        )
    }
}
