package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.EmojiFeature;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {
    @ModifyVariable(method = "getVisualOrder", at = @At("HEAD"), argsOnly = true, name = "logicalOrderText")
    private FormattedText rfu$replaceLogicalEmojis(FormattedText logicalOrderText) {
        return EmojiFeature.replaceEmojisInText(logicalOrderText);
    }
}
