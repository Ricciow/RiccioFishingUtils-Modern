package cloud.glitchdev.rfu.gui.components.elementa

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.constants.text.EmojiData
import gg.essential.elementa.UIComponent
import gg.essential.elementa.dsl.basicWidthConstraint
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.renderer.ElementaExtractor
import gg.essential.universal.UGraphics
import gg.essential.universal.render.UGpuSampler
import kotlin.math.roundToInt

/** An explicit atlas icon; text aliases and the emoji replacement toggle do not apply. */
class UIEmoji(private val emoji: EmojiData, scale: Float = 1f) : UIComponent() {
    private val font = requireNotNull(emoji.font)

    init {
        constrain {
            width = basicWidthConstraint { mc.font.width(emoji.component()).toFloat() * scale }
            height = (8f * scale).pixels()
        }
    }

    override fun extractComponent(extractor: ElementaExtractor) {
        val atlas = mc.atlasManager.getAtlasOrThrow(font.atlasId())
        val sprite = atlas.getSprite(font.spriteId())
        val scale = getHeight() / 8f
        val height = emoji.height * scale
        val width = sprite.contents().width() * height / sprite.contents().height()
        val x = getLeft()
        val y = getTop() + (7 - emoji.ascent) * scale
        extractor.blit(
            (x * extractor.guiScale).roundToInt(), (y * extractor.guiScale).roundToInt(),
            ((x + width) * extractor.guiScale).roundToInt(), ((y + height) * extractor.guiScale).roundToInt(),
            sprite.u0, sprite.v0, sprite.u1, sprite.v1,
            UGraphics.getPlatformAdapter().textureView(atlas.textureView),
            UGpuSampler(
                UGpuSampler.AddressMode.CLAMP_TO_EDGE, UGpuSampler.AddressMode.CLAMP_TO_EDGE,
                UGpuSampler.FilterMode.NEAREST, UGpuSampler.FilterMode.NEAREST, false,
            ),
            textureContentImmutable = false, premultipliedAlpha = false, color = getColor(),
        )
    }
}
