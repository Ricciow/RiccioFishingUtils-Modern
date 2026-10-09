package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.EmojiFeature;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public class FontMixin {
    @Inject(method = "prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At("HEAD"), cancellable = true)
    private void rfu$prepareSpriteText(String text, float x, float y, int originalColor, boolean drawShadow, int backgroundColor, CallbackInfoReturnable<Font.PreparedText> cir) {
        if (EmojiFeature.hasEmojis(text)) {
            FormattedCharSequence sequence = Language.getInstance().getVisualOrder(FormattedText.of(text));
            cir.setReturnValue(((Font) (Object) this).prepareText(sequence, x, y, originalColor, drawShadow, false, backgroundColor));
        }
    }
}
