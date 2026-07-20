package cloud.glitchdev.rfu.constants.ui

enum class TooltipGuiScale(val displayName: String, val scaleValue: Int?) {
    DEFAULT("Default", null),
    DYNAMIC("Dynamic", null),
    SCALE_1("1x", 1),
    SCALE_2("2x", 2),
    SCALE_3("3x", 3),
    SCALE_4("4x", 4);

    override fun toString(): String = displayName
}
