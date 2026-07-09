package cloud.glitchdev.rfu.constants.skyblock

enum class Mayors(val mayorName: String) {
    AATROX("Aatrox"),
    COLE("Cole"),
    DIANA("Diana"),
    DIAZ("Diaz"),
    FINNEGAN("Finnegan"),
    FOXY("Foxy"),
    MARINA("Marina"),
    PAUL("Paul"),
    JERRY("Jerry"),
    DERPY("Derpy"),
    SCORPIUS("Scorpius"),
    UNKNOWN("Unknown");

    companion object {
        fun fromName(name: String): Mayors {
            return entries.find { it.mayorName.equals(name, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
