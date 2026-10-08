package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.EmojiFeature;
import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringSplitter.class)
public abstract class StringSplitterMixin {
    @Shadow @Final private StringSplitter.WidthProvider widthProvider;
    @Shadow public abstract float stringWidth(FormattedCharSequence text);

    @Inject(method = "stringWidth(Ljava/lang/String;)F", at = @At("HEAD"), cancellable = true)
    private void rfu$measureSpriteString(String str, CallbackInfoReturnable<Float> cir) {
        if (str == null) return;
        FormattedCharSequence sequence = EmojiFeature.replaceEmojisInString(str, Style.EMPTY);
        if (sequence != null) cir.setReturnValue(this.stringWidth(sequence));
    }

    @ModifyVariable(method = {
        "stringWidth(Lnet/minecraft/network/chat/FormattedText;)F",
        "headByWidth(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/network/chat/Style;)Lnet/minecraft/network/chat/FormattedText;",
        "splitLines(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/network/chat/Style;Ljava/util/function/BiConsumer;)V"
    }, at = @At("HEAD"), argsOnly = true)
    private FormattedText rfu$layoutSpriteText(FormattedText text) {
        return EmojiFeature.replaceEmojisInText(text);
    }

    @Inject(method = "plainIndexAtWidth", at = @At("HEAD"), cancellable = true)
    private void rfu$trimSpriteHead(String str, int maxWidth, Style style, CallbackInfoReturnable<Integer> cir) {
        Integer position = EmojiFeature.plainIndexAtWidth(str, maxWidth, style, this.widthProvider, false);
        if (position != null) cir.setReturnValue(position);
    }

    @Inject(method = "plainTailByWidth", at = @At("HEAD"), cancellable = true)
    private void rfu$trimSpriteTail(String str, int maxWidth, Style style, CallbackInfoReturnable<String> cir) {
        Integer position = EmojiFeature.plainIndexAtWidth(str, maxWidth, style, this.widthProvider, true);
        if (position != null) cir.setReturnValue(str.substring(position));
    }
}
