package cloud.glitchdev.rfu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.Font$PreparedTextBuilder")
public class FontPreparedTextBuilderMixin {
    @WrapOperation(
        method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;createGlyph(FFIILnet/minecraft/network/chat/Style;FF)Lnet/minecraft/client/gui/font/TextRenderable$Styled;")
    )
    private TextRenderable.Styled rfu$alignEmojiBaseline(BakedGlyph glyph, float x, float y, int color, int shadowColor, Style style, float boldOffset, float shadowOffset, Operation<TextRenderable.Styled> original) {
        if (style.getFont() instanceof FontDescription.AtlasSprite(Identifier atlasId, Identifier spriteId)
            && atlasId.getNamespace().equals("minecraft") && atlasId.getPath().equals("gui")
            && spriteId.getNamespace().equals("rfu") && spriteId.getPath().startsWith("emoji/")) {
            y += 1f;
        }
        return original.call(glyph, x, y, color, shadowColor, style, boldOffset, shadowOffset);
    }
}
