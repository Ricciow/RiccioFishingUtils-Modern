package cloud.glitchdev.rfu.gui.hud

import gg.essential.elementa.effects.Effect
import gg.essential.elementa.renderer.ElementaExtractor

class HudRenderPassEffect(private val element: AbstractHudElement) : Effect() {
    private var isScissored = false

    override fun extractBefore(extractor: ElementaExtractor) {
        if (!element.shouldDrawInCurrentPass()) {
            extractor.pushScissor(0, 0, 0, 0)
            isScissored = true
        }
    }

    override fun extractAfter(extractor: ElementaExtractor) {
        if (isScissored) {
            extractor.popScissor()
            isScissored = false
        }
    }
}
