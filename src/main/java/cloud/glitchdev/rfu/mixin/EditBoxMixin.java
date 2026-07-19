package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.EmojiFeature;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EditBox.class)
public abstract class EditBoxMixin {
    @Shadow private String value;
    @Shadow private int displayPos;
    @Shadow private int cursorPos;
    @Shadow private int highlightPos;
    @Shadow private int textX;
    @Shadow @Final private Font font;
    @Shadow public abstract int getInnerWidth();
    @Shadow protected abstract FormattedCharSequence applyFormat(String text, int offset);

    @Inject(method = "applyFormat", at = @At("RETURN"), cancellable = true)
    private void rfu$replaceEmojisInEditBox(String text, int offset, CallbackInfoReturnable<FormattedCharSequence> cir) {
        FormattedCharSequence original = cir.getReturnValue();
        FormattedCharSequence replaced = EmojiFeature.INSTANCE.replaceEmojisInCharSequence(original);
        if (replaced != null && replaced != original) {
            cir.setReturnValue(replaced);
        }
    }

    @Redirect(
        method = "extractWidgetRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I"
        )
    )
    private int rfu$redirectWidthInExtractWidgetRenderState(Font font, String str) {
        FormattedCharSequence formatted = this.applyFormat(str, this.displayPos);
        return font.width(formatted);
    }

    @Inject(method = "findClickedPositionInText", at = @At("HEAD"), cancellable = true)
    private void rfu$onFindClickedPositionInText(MouseButtonEvent event, CallbackInfoReturnable<Integer> cir) {
        int positionInText = Math.min(Mth.floor(event.x()) - this.textX, this.getInnerWidth());
        String displayed = this.value.substring(this.displayPos);
        int pos = this.displayPos + EmojiFeature.getClickedRawPosition(this.font, displayed, positionInText);
        cir.setReturnValue(pos);
    }

    @ModifyVariable(method = "setCursorPosition", at = @At("HEAD"), argsOnly = true, name = "pos")
    private int rfu$snapCursorPosition(int pos) {
        int refAnchor = (this.highlightPos != this.cursorPos) ? this.highlightPos : this.cursorPos;
        boolean preferEnd = pos >= refAnchor;
        return EmojiFeature.snapToEmojiBoundary(this.value, pos, preferEnd);
    }

    @ModifyVariable(method = "setHighlightPos", at = @At("HEAD"), argsOnly = true, name = "pos")
    private int rfu$snapHighlightPos(int pos) {
        int refAnchor = (this.highlightPos != this.cursorPos) ? this.cursorPos : this.highlightPos;
        boolean preferEnd = pos >= refAnchor;
        return EmojiFeature.snapToEmojiBoundary(this.value, pos, preferEnd);
    }
}
