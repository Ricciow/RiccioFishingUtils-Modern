package cloud.glitchdev.rfu.gui.window

//? if >= 26.3 {
import gg.essential.elementa.renderer.ElementaRenderer
import gg.essential.universal.render.UGpuFormat
import gg.essential.universal.render.UGpuTexture
import gg.essential.universal.render.UGpuTextureView
import gg.essential.universal.UGraphics
import gg.essential.universal.UScreen

class HudMultiPassRenderer : UScreen.Renderer {
    private val elementaRenderer = ElementaRenderer()
    private var lastWidth = 0
    private var lastHeight = 0
    private val textures = mutableListOf<UGpuTextureView>()
    private var textureIndex = 0

    fun onFrameStart() {
        textureIndex = 0
    }

    override fun render(state: UScreen.RenderState): UGpuTextureView {
        state as HudMultiPassRenderState

        val width = if (state.renderState.screenWidth > 0) state.renderState.screenWidth else 1
        val height = if (state.renderState.screenHeight > 0) state.renderState.screenHeight else 1

        if (lastWidth != width || lastHeight != height) {
            lastWidth = width
            lastHeight = height
            for (tv in textures) {
                tv.texture.close()
                tv.close()
            }
            textures.clear()
            textureIndex = 0
        }

        val textureView = if (textureIndex < textures.size) {
            textures[textureIndex]
        } else {
            val device = UGraphics.getDevice()
            val texture = device.createTexture(
                null,
                UGpuTexture.Usage.TEXTURE_BINDING + UGpuTexture.Usage.RENDER_ATTACHMENT,
                UGpuFormat.DEFAULT_RGBA,
                width,
                height,
                1,
            )
            val tv = device.createTextureView(texture, 0, 1)
            textures.add(tv)
            tv
        }
        textureIndex++

        elementaRenderer.renderToTexture(
            textureView,
            0, 0,
            0, 0,
            width, height,
            state.renderState
        )

        return textureView
    }

    override fun close() {
        for (tv in textures) {
            tv.texture.close()
            tv.close()
        }
        textures.clear()
        elementaRenderer.close()
    }
}
//?}
