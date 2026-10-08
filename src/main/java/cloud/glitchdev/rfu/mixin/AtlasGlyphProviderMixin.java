package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.constants.text.Emoji;
import cloud.glitchdev.rfu.constants.text.EmojiData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.font.AtlasGlyphProvider;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.SingleSpriteSource;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AtlasGlyphProvider.class)
public class AtlasGlyphProviderMixin {
    @Shadow @Final private TextureAtlas atlas;
    @Shadow @Final private GlyphRenderTypes renderTypes;

    @WrapOperation(
        method = "createSprite",
        at = @At(value = "NEW", target = "net/minecraft/client/gui/font/SingleSpriteSource")
    )
    private SingleSpriteSource rfu$sizeEmojiGlyph(BakedGlyph glyph, Operation<SingleSpriteSource> original, @Local(argsOnly = true, name = "sprite") TextureAtlasSprite sprite) {
        if (!this.atlas.location().equals(Sheets.GUI_SHEET)) return original.call(glyph);
        SpriteContents contents = sprite.contents();
        EmojiData emoji = Emoji.getSprite(contents.name());
        if (emoji == null) return original.call(glyph);

        float height = emoji.getHeight();
        float width = contents.width() * height / contents.height();
        int advance = Math.round(rfu$opaqueWidth(contents) * width / contents.width()) + 1;
        float top = 7f - emoji.getAscent();
        return original.call(new BakedSheetGlyph(
            GlyphInfo.simple(advance), this.renderTypes, this.atlas.getTextureView(),
            sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(),
            0f, width, top, top + height
        ));
    }

    @Unique
    private static int rfu$opaqueWidth(SpriteContents contents) {
        for (int x = contents.width() - 1; x >= 0; x--) {
            for (int y = 0; y < contents.height(); y++) {
                if (!contents.isTransparent(0, x, y)) return x + 1;
            }
        }
        return 0;
    }
}