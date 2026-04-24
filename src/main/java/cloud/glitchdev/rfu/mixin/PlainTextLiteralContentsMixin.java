package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.EmojiFeature;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlainTextContents.LiteralContents.class)
public class PlainTextLiteralContentsMixin {
    @ModifyVariable(method = "<init>(Ljava/lang/String;)V", at = @At("HEAD"), argsOnly = true, name = "text")
    private static String rfu$modifyText(String text) {
        return EmojiFeature.INSTANCE.replaceEmojis(text);
    }
}
