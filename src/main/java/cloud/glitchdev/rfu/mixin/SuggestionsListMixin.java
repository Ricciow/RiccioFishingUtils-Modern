package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.EmojiSuggestion;
import com.mojang.brigadier.suggestion.Suggestion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.gui.components.CommandSuggestions$SuggestionsList")
public class SuggestionsListMixin {
    @Redirect(
        method = "extractRenderState",
        at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/suggestion/Suggestion;getText()Ljava/lang/String;")
    )
    private String rfu$redirectGetTextInExtractRenderState(Suggestion suggestion) {
        if (suggestion instanceof EmojiSuggestion emojiSuggestion) {
            return emojiSuggestion.getDisplayText();
        }
        return suggestion.getText();
    }
}
