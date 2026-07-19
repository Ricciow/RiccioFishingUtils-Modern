package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.EmojiFeature;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EditBox.class)
public class EditBoxMixin {
    @Inject(method = "applyFormat", at = @At("RETURN"), cancellable = true)
    private void rfu$replaceEmojisInEditBox(String text, int offset, CallbackInfoReturnable<FormattedCharSequence> cir) {
        FormattedCharSequence original = cir.getReturnValue();
        FormattedCharSequence replaced = EmojiFeature.INSTANCE.replaceEmojisInCharSequence(original);
        if (replaced != null && replaced != original) {
            cir.setReturnValue(replaced);
        }
    }
}
