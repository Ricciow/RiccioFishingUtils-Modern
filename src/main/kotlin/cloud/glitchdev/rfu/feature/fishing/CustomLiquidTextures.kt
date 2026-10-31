package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.access.LiquidPaletteSprite
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.constants.fishing.LiquidTypes
import cloud.glitchdev.rfu.mixin.SpriteAnimationStateAccessor
import cloud.glitchdev.rfu.mixin.TextureAtlasSpriteAccessor
import cloud.glitchdev.rfu.utils.rendering.LavaColorPalette
import cloud.glitchdev.rfu.utils.rendering.LiquidTexturePixels
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
//~ if >=26.3 'com.mojang.blaze3d.textures' -> 'com.mojang.renderpearl.api.textures' {
import com.mojang.renderpearl.api.textures.GpuTexture
//~}
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.resources.Identifier
import kotlin.math.roundToInt

object CustomLiquidTextures {
    private val blockAtlas = Identifier.withDefaultNamespace("textures/atlas/blocks.png")
    private data class Style(val owner: LiquidTypes, val appearance: LiquidTypes)
    private data class Colors(val palette: LavaColorPalette?)

    private val sprites = buildMap {
        for (owner in LiquidTypes.entries) {
            for (appearance in LiquidTypes.entries) {
                val parts = if (appearance == LiquidTypes.WATER) listOf("still", "flow", "overlay") else listOf("still", "flow")
                if (appearance == LiquidTypes.WATER && owner == LiquidTypes.LAVA) continue
                for (part in parts) put(spriteId(owner, appearance, part), Style(owner, appearance))
            }
        }
    }

    fun spriteId(owner: LiquidTypes, appearance: LiquidTypes, part: String): Identifier {
        val name = if (appearance == LiquidTypes.WATER) "water" else "${owner.name.lowercase()}_lava"
        return Identifier.fromNamespaceAndPath("rfu", "block/custom_${name}_$part")
    }

    fun opacity(owner: LiquidTypes, appearance: LiquidTypes): Int {
        val percent = if (owner == LiquidTypes.WATER) {
            if (appearance == LiquidTypes.WATER) GeneralFishing.waterTextureOpacity else GeneralFishing.lavaTextureOpacity
        } else {
            if (appearance == LiquidTypes.WATER) LavaFishing.waterTextureOpacity else LavaFishing.lavaTextureOpacity
        }
        return (percent.coerceIn(0f, 100f) / 100f * 255f).roundToInt()
    }

    private fun colors(style: Style): Colors {
        val custom = if (style.owner == LiquidTypes.WATER) GeneralFishing.customWaterCustomColor else LavaFishing.customLavaCustomColor
        val palette = if (!custom || style.appearance != LiquidTypes.LAVA) null else if (style.owner == LiquidTypes.WATER) {
            LavaColorPalette(GeneralFishing.customWaterDarkColor, GeneralFishing.customWaterMidtoneColor, GeneralFishing.customWaterHighlightColor)
        } else {
            LavaColorPalette(LavaFishing.customLavaDarkColor, LavaFishing.customLavaMidtoneColor, LavaFishing.customLavaHighlightColor)
        }
        return Colors(palette)
    }

    fun create(name: Identifier, image: NativeImage): PaletteImage? =
        sprites[name]?.let { PaletteImage(name, image, if (it.appearance == LiquidTypes.WATER) 180 else 255) }

    class PaletteImage internal constructor(private val name: Identifier, image: NativeImage, vanillaAlpha: Int) {
        private val pixels = LiquidTexturePixels(image.pixels, vanillaAlpha)
        private var previous: Colors? = null

        fun refresh(image: NativeImage): Boolean {
            val configured = colors(sprites.getValue(name))
            if (configured == previous) return false
            val recolored = pixels.recolor(configured.palette, 255)
            for (y in 0 until image.height) {
                for (x in 0 until image.width) image.setPixel(x, y, recolored[y * image.width + x])
            }
            previous = configured
            return true
        }
    }

    fun refresh(atlas: TextureAtlas) {
        if (atlas.location() != blockAtlas) return
        for (id in sprites.keys) {
            val sprite = atlas.getSprite(id)
            if (sprite.contents().name() != id) continue
            val mutable = sprite.contents() as LiquidPaletteSprite
            val mips = mutable.`rfu$refreshPalette`() ?: continue
            val animation = mutable.`rfu$getAnimationState`()
            if (animation != null) {
                val state = animation as SpriteAnimationStateAccessor
                val columns = mips[0].width / sprite.contents().width()
                for (entry in state.`rfu$getFrameTextures`().int2ObjectEntrySet()) {
                    val frameX = entry.intKey % columns * sprite.contents().width()
                    val frameY = entry.intKey / columns * sprite.contents().height()
                    for (level in mips.indices) {
                        region(mips[level], frameX shr level, frameY shr level,
                            maxOf(1, sprite.contents().width() shr level), maxOf(1, sprite.contents().height() shr level)).use {
                            upload(entry.value.texture(), it, level, 0, 0)
                        }
                    }
                }
                state.`rfu$setDirty`(true)
            } else {
                val padding = (sprite as TextureAtlasSpriteAccessor).`rfu$getPadding`()
                for (level in mips.indices) {
                    val width = maxOf(1, (sprite.contents().width() + padding * 2) shr level)
                    val height = maxOf(1, (sprite.contents().height() + padding * 2) shr level)
                    region(mips[level], -(padding shr level), -(padding shr level), width, height).use {
                        upload(atlas.texture, it, level, sprite.x shr level, sprite.y shr level)
                    }
                }
            }
        }
    }

    private fun region(source: NativeImage, x: Int, y: Int, width: Int, height: Int): NativeImage {
        val image = NativeImage(width, height, false)
        for (dy in 0 until height) {
            for (dx in 0 until width) {
                image.setPixel(dx, dy, source.getPixel((x + dx).coerceIn(0, source.width - 1), (y + dy).coerceIn(0, source.height - 1)))
            }
        }
        return image
    }

    private fun upload(texture: GpuTexture, image: NativeImage, level: Int, x: Int, y: Int) {
        val encoder = RenderSystem.getDevice().createCommandEncoder()
        //? if >=26.2 {
        encoder.writeToTexture(texture, image, level, 0, x, y)
        //?} else {
        /*encoder.writeToTexture(texture, image, level, 0, x, y, image.width, image.height, 0, 0)
        *///?}
    }
}
