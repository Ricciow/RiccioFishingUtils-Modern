package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.EmojiSuggestion;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.input.KeyEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(targets = "net.minecraft.client.gui.components.CommandSuggestions$SuggestionsList")
public abstract class SuggestionsListMixin {
    @Shadow @Final
    CommandSuggestions this$0;
    @Shadow @Final
    private List<Suggestion> suggestionList;
    @Shadow private int current;
    @Shadow public abstract void useSuggestion();

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void rfu$onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (event.isConfirmation()) {
            if (this.current >= 0 && this.current < this.suggestionList.size()) {
                Suggestion suggestion = this.suggestionList.get(this.current);
                if (suggestion instanceof EmojiSuggestion) {
                    this.useSuggestion();
                    this$0.hide();
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @WrapOperation(
        method = "extractRenderState",
        at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/suggestion/Suggestion;getText()Ljava/lang/String;")
    )
    private String rfu$redirectGetTextInExtractRenderState(Suggestion suggestion, Operation<String> original) {
        if (suggestion instanceof EmojiSuggestion emojiSuggestion) {
            return emojiSuggestion.getDisplayText();
        }
        return original.call(suggestion);
    }
}
