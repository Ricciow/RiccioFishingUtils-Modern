package cloud.glitchdev.rfu.mixin.universalcraft;

import cloud.glitchdev.rfu.constants.text.Emoji;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.essential.universal.render.font.UFontRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.network.chat.FontDescription;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = UFontRenderer.class, remap = false)
public class UFontRendererMixin {
    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font$PreparedText;visit(Lnet/minecraft/client/gui/Font$GlyphVisitor;)V",
            ordinal = 0
        )
    )
    private void rfu$skipEmojiInCustomShadow(Font.PreparedText text, Font.GlyphVisitor visitor, Operation<Void> original) {
        original.call(text, new Font.GlyphVisitor() {
            @Override
            public void acceptGlyph(TextRenderable.@NonNull Styled glyph) {
                if (glyph.style().getFont() instanceof FontDescription.AtlasSprite sprite
                    && Emoji.getSprite(sprite.spriteId()) != null) {
                    return;
                }
                visitor.acceptGlyph(glyph);
            }

            @Override
            public void acceptEffect(@NonNull TextRenderable effect) {
                visitor.acceptEffect(effect);
            }
        });
    }
}
