package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.EmojiFeature;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.client.gui.Font$PreparedTextBuilder")
public class FontPreparedTextBuilderMixin {
    @ModifyVariable(
            method = "accept(ILnet/minecraft/network/chat/Style;I)Z",
            at = @At("HEAD"),
            argsOnly = true,
            name = "style"
    )
    private Style rfu$removeShadowForEmoji(Style style, int position, Style styleArg, int codepoint) {
        if (EmojiFeature.isEmojiCodepoint(codepoint)) {
            return style.withShadowColor(0);
        }
        return style;
    }
}
