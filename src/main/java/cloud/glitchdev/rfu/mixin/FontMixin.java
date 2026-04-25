package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.EmojiFeature;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public class FontMixin {
    @ModifyVariable(method = "prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At("HEAD"), argsOnly = true, name = "text")
    private String rfu$replaceEmojisInPrepareText(String text) {
        return EmojiFeature.INSTANCE.replaceEmojis(text);
    }

    @ModifyVariable(method = "width(Ljava/lang/String;)I", at = @At("HEAD"), argsOnly = true, name = "str")
    private String rfu$replaceEmojisInWidth(String str) {
        return EmojiFeature.INSTANCE.replaceEmojis(str);
    }
}
