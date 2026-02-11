package cloud.glitchdev.rfu.utils.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

class StringListArgumentType(
    private val stringList: List<String>,
    private val greedy: Boolean = false
) : ArgumentType<String> {
    constructor(vararg strings : String, greedy: Boolean = false) : this(strings.toList(), greedy)

    override fun parse(reader: StringReader): String {
        val input = if(greedy) reader.remaining else reader.readString()

        if(greedy) {
            reader.cursor = reader.totalLength
        }

        return stringList.find { it == input }
            ?: throw SimpleCommandExceptionType(
                Component.literal(
                    "Invalid argument: '$input'.",
                )
            ).createWithContext(reader)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(stringList, builder)
    }
}