package cloud.glitchdev.rfu.constants

enum class InkTrackingType(val displayName: String) {
    INK_H("Ink/h"),
    UPTIME("Ink uptime"),
    INK_TOT("Ink (total)"),
    N_SQUID("Night Squids"),
    SQUIDS("Squids"),
    INK_GOAL("Ink Goal"),
    ETA("Goal ETA"),
    OVERALL("Overall");

    override fun toString(): String {
        return displayName
    }
}
