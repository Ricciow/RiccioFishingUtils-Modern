package cloud.glitchdev.rfu.gui.window

//? if >= 26.3 {
import gg.essential.elementa.renderer.ElementaRenderState
import gg.essential.universal.UScreen

class HudMultiPassRenderState(
    val renderState: ElementaRenderState,
    override val background: Boolean = false
) : UScreen.RenderState
//?}
