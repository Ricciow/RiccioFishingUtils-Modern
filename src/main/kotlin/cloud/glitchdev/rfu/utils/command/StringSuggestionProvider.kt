package cloud.glitchdev.rfu.utils.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.SharedSuggestionProvider
import java.util.concurrent.CompletableFuture

class StringSuggestionProvider(
    val strings : List<String>
) : SuggestionProvider<FabricClientCommandSource> {
    override fun getSuggestions(
        context: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(strings, builder)
    }
}