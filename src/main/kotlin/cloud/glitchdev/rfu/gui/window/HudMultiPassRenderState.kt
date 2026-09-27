package cloud.glitchdev.rfu.gui.window

import gg.essential.elementa.renderer.ElementaRenderState
import gg.essential.universal.UScreen

class HudMultiPassRenderState(
    val renderState: ElementaRenderState,
    override val background: Boolean = false
) : UScreen.RenderState
