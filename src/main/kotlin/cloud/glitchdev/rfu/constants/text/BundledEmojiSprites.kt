package cloud.glitchdev.rfu.constants.text

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.MapCodec
import net.fabricmc.fabric.api.client.rendering.v1.SpriteSourceRegistry
import net.minecraft.client.renderer.texture.SpriteContents
import net.minecraft.client.renderer.texture.atlas.SpriteSource
import net.minecraft.client.resources.metadata.animation.FrameSize
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager

internal object BundledEmojiSprites : SpriteSource {
    private val codec: MapCodec<BundledEmojiSprites> = MapCodec.unit(this)

    fun register() {
        SpriteSourceRegistry.register(Identifier.fromNamespaceAndPath("rfu", "bundled_emojis"), codec)
    }

    override fun codec(): MapCodec<out SpriteSource> = codec

    override fun run(resourceManager: ResourceManager, output: SpriteSource.Output) {
        for (entry in EmojiPack.entries) {
            val id = entry.emoji.font?.spriteId() ?: continue
            val texture = checkNotNull(entry.texture)
            output.add(id) {
                val image = NativeImage.read(texture)
                SpriteContents(id, FrameSize(image.width, image.height), image)
            }
        }
    }
}