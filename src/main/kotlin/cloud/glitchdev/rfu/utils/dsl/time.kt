package cloud.glitchdev.rfu.utils.dsl

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.toJavaInstant

fun Instant.toFormattedDate() : String {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    return formatter.format(this)
}

fun kotlin.time.Instant.toFormattedDate() : String {
    return this.toJavaInstant().toFormattedDate()
}

fun Duration.toReadableString(ms: Boolean = false): String {
    return toComponents { days, hours, minutes, seconds, nanoseconds ->
        buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0 && days == 0L) append("${minutes}m ")
            if ((seconds > 0 && days == 0L) || isEmpty()) {
                if (!ms || minutes > 1) {
                    append("${seconds}s")
                } else {
                    val centiseconds = (nanoseconds / 10_000_000).toString().padStart(2, '0')
                    append("$seconds.${centiseconds}s")
                }
            }
        }.trim()
    }
}