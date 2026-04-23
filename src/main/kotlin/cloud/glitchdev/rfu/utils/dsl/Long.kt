package cloud.glitchdev.rfu.utils.dsl

fun Long.compact(): String {
    return when {
        this >= 1_000_000_000L -> "${String.format("%.1f", this / 1_000_000_000.0)}B"
        this >= 1_000_000L -> "${String.format("%.1f", this / 1_000_000.0)}M"
        this >= 10_000L -> "${String.format("%.1f", this / 1_000.0)}k"
        else -> "%,d".format(this)
    }.replace(".0", "")
}
