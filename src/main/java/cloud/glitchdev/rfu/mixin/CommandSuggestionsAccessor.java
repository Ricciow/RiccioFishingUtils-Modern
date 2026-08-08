package cloud.glitchdev.rfu.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public interface CommandSuggestionsAccessor {
    @Accessor("input")
    EditBox rfu$getInput();

    @Accessor("pendingSuggestions")
    void rfu$setPendingSuggestions(CompletableFuture<Suggestions> pendingSuggestions);
}
