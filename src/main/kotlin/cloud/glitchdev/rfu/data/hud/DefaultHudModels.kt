package cloud.glitchdev.rfu.data.hud

enum class HudAnchor {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    MIDDLE_LEFT,
    CENTER,
    MIDDLE_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT
}

data class DefaultHudElementEntry(
    val anchor: HudAnchor = HudAnchor.TOP_LEFT,
    val x: Float = 0f,
    val y: Float = 0f,
    val scale: Float = 1f,
    val width: Float = 0f,
    val height: Float = 0f
)

data class DefaultHudLayout(
    val referenceWidth: Float = 960f,
    val referenceHeight: Float = 540f,
    val elements: Map<String, DefaultHudElementEntry> = emptyMap()
)

data class CalculatedHudPosition(
    val x: Float,
    val y: Float,
    val scale: Float
)
