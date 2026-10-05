package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.renderer.ElementaExtractor
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import kotlin.math.floor

class UIVanillaTooltip(
    var tooltip: () -> List<Component>,
    var tooltipStyle: Identifier? = null,
) : UIContainer() {
    constructor(lines: List<Component>, tooltipStyle: Identifier? = null) : this({ lines }, tooltipStyle)

    constructor(text: Component, tooltipStyle: Identifier? = null) : this(listOf(text), tooltipStyle)

    var tooltipEnabled = true

    init {
        constrain {
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }
    }

    override fun extractComponent(extractor: ElementaExtractor) {
        if (tooltipEnabled && isHovered()) {
            val (mouseX, mouseY) = getMousePosition()
            val pixelX = floor(mouseX * extractor.guiScale).toInt()
            val pixelY = floor(mouseY * extractor.guiScale).toInt()
            if (extractor.isVisible(pixelX, pixelY, pixelX + 1, pixelY + 1)) {
                TooltipRenderer.submit(tooltip(), tooltipStyle)
            }
        }
        super.extractComponent(extractor)
    }
}
