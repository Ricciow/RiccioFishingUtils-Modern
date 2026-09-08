package cloud.glitchdev.rfu.utils.command.arguments

import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.network.chat.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture
import kotlin.time.Clock
import kotlin.time.Instant

class DateArgumentType(
    private val suggestions: List<String> = listOf("now", "today")
) : ArgumentType<Instant> {

    companion object {
        private val INVALID_DATE = SimpleCommandExceptionType(
            Component.literal("${LIGHT_RED}Invalid date! Example: dd/mm/yyyy or dd/mm/yyyy:hh:mm")
        )

        private val dateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yyyy:H:m")
        private val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")

        fun date(suggestions: List<String> = listOf("now", "today", "dd/mm/yyyy", "dd/mm/yyyy:hh:mm")): DateArgumentType = DateArgumentType(suggestions)

        fun <S> getDate(context: CommandContext<S>, name: String): Instant {
            return context.getArgument(name, Instant::class.java)
        }

        fun parseDate(input: String): Instant? {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return null

            if (trimmed.equals("now", ignoreCase = true) || trimmed == "-" || trimmed.equals("today", ignoreCase = true)) {
                return Clock.System.now()
            }

            val zone = ZoneId.systemDefault()

            try {
                val ldt = LocalDateTime.parse(trimmed, dateTimeFormatter)
                val javaInstant = ldt.atZone(zone).toInstant()
                return Instant.fromEpochMilliseconds(javaInstant.toEpochMilli())
            } catch (_: Exception) {}

            try {
                val ld = LocalDate.parse(trimmed, dateFormatter)
                val javaInstant = ld.atStartOfDay(zone).toInstant()
                return Instant.fromEpochMilliseconds(javaInstant.toEpochMilli())
            } catch (_: Exception) {}

            return null
        }
    }

    override fun parse(reader: StringReader): Instant {
        val start = reader.cursor
        val input = if (reader.canRead() && (reader.peek() == '"' || reader.peek() == '\'')) {
            reader.readQuotedString()
        } else {
            while (reader.canRead() && reader.peek() != ' ') {
                reader.skip()
            }
            reader.string.substring(start, reader.cursor)
        }

        return parseDate(input) ?: throw INVALID_DATE.createWithContext(reader)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val remaining = builder.remainingLowerCase

        suggestions.forEach {
            if (it.lowercase().contains(remaining)) {
                builder.suggest(it)
            }
        }

        return builder.buildFuture()
    }
}
