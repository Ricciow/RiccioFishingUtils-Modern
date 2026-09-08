package cloud.glitchdev.rfu.utils.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
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
        private val INVALID_DATE = DynamicCommandExceptionType { input ->
            Component.literal("Invalid date format '$input'. Example formats: 18/12/2026, 18/12/2026 18:00, or 'now'")
        }

        private val dateTimeFormatters = listOf(
            DateTimeFormatter.ofPattern("d/M/yyyy H:m:s"),
            DateTimeFormatter.ofPattern("d/M/yyyy H:m"),
            DateTimeFormatter.ofPattern("d-M-yyyy H:m:s"),
            DateTimeFormatter.ofPattern("d-M-yyyy H:m"),
            DateTimeFormatter.ofPattern("yyyy-M-d H:m:s"),
            DateTimeFormatter.ofPattern("yyyy-M-d H:m"),
            DateTimeFormatter.ofPattern("yyyy/M/d H:m:s"),
            DateTimeFormatter.ofPattern("yyyy/M/d H:m"),
        )

        private val dateFormatters = listOf(
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-M-d"),
            DateTimeFormatter.ofPattern("yyyy/M/d"),
        )

        fun date(suggestions: List<String> = listOf("now", "today")): DateArgumentType = DateArgumentType(suggestions)

        fun <S> getDate(context: CommandContext<S>, name: String): Instant {
            return context.getArgument(name, Instant::class.java)
        }

        fun parseDate(input: String): Instant? {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return null

            if (trimmed.equals("now", ignoreCase = true) || trimmed == "-" || trimmed.equals("today", ignoreCase = true)) {
                return Clock.System.now()
            }

            trimmed.toLongOrNull()?.let { num ->
                return if (num > 100_000_000_000L) {
                    Instant.fromEpochMilliseconds(num)
                } else {
                    Instant.fromEpochSeconds(num)
                }
            }

            runCatching {
                return Instant.parse(trimmed)
            }

            val zone = ZoneId.systemDefault()

            for (formatter in dateTimeFormatters) {
                try {
                    val ldt = LocalDateTime.parse(trimmed, formatter)
                    val javaInstant = ldt.atZone(zone).toInstant()
                    return Instant.fromEpochMilliseconds(javaInstant.toEpochMilli())
                } catch (_: Exception) {}
            }

            for (formatter in dateFormatters) {
                try {
                    val ld = LocalDate.parse(trimmed, formatter)
                    val javaInstant = ld.atStartOfDay(zone).toInstant()
                    return Instant.fromEpochMilliseconds(javaInstant.toEpochMilli())
                } catch (_: Exception) {}
            }

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

        return parseDate(input) ?: throw INVALID_DATE.createWithContext(reader, input)
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
