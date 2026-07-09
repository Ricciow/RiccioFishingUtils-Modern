package cloud.glitchdev.rfu.constants.fishing

enum class RareScDisplayDataType(val displayName: String) {
    STREAK("Count"),
    AVERAGE("Average"),
    TOTAL("Total"),
    TIME_SINCE("Time Since");

    override fun toString(): String = displayName
}
