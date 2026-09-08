package cloud.glitchdev.rfu.utils.dsl

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.Instant

object DateParser {
    private val dateTimeFormatters = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy")
    )

    fun parse(input: String): Instant? {
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

        return null
    }
}
