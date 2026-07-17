package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.EmojiAutocomplete;
import cloud.glitchdev.rfu.feature.other.EmojiSuggestion;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Shadow @Final
    private EditBox input;
    @Shadow @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow
    private boolean allowSuggestions;
    @Shadow @Final
    private Minecraft minecraft;
    @Shadow
    private CommandSuggestions.@Nullable SuggestionsList suggestions;
    @Shadow
    public abstract void showSuggestions(boolean immediateNarration);

    @Inject(method = "updateCommandInfo", at = @At("TAIL"))
    private void rfu$onUpdateCommandInfo(CallbackInfo ci) {
        CompletableFuture<Suggestions> emojiFuture = EmojiAutocomplete.getEmojiSuggestions(this.input.getValue(), this.input.getCursorPosition());
        if (emojiFuture != null) {
            this.pendingSuggestions = emojiFuture;
            this.suggestions = null;
            if (this.allowSuggestions && this.minecraft.options.autoSuggestions().get()) {
                this.showSuggestions(false);
            }
        }
    }

    @Redirect(
        method = "showSuggestions",
        at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/suggestion/Suggestion;getText()Ljava/lang/String;")
    )
    private String rfu$redirectGetTextInShowSuggestions(Suggestion suggestion) {
        if (suggestion instanceof EmojiSuggestion emojiSuggestion) {
            return emojiSuggestion.getDisplayText();
        }
        return suggestion.getText();
    }

    @Redirect(
        method = "showSuggestions",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I")
    )
    private int rfu$redirectWidthInShowSuggestions(Font font, String text) {
        return font.width(FormattedText.of(text));
    }
}
