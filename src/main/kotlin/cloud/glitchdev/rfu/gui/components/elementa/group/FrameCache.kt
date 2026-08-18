package cloud.glitchdev.rfu.gui.components.elementa.group

internal class FrameCache {
    private var currentFrameTime: Long = -1L
    private var currentFrameMax: Float = 0f
    private var lastFrameMax: Float = 0f

    @Synchronized
    fun updateAndGetMax(frameTime: Long, baseValue: Float): Float {
        if (frameTime != currentFrameTime) {
            if (currentFrameTime != -1L && frameTime > currentFrameTime) {
                lastFrameMax = currentFrameMax
            } else if (frameTime < currentFrameTime) {
                lastFrameMax = 0f
            }
            currentFrameTime = frameTime
            currentFrameMax = baseValue
        } else {
            if (baseValue > currentFrameMax) {
                currentFrameMax = baseValue
            }
        }

        val effectiveMax = if (lastFrameMax > currentFrameMax) lastFrameMax else currentFrameMax
        return if (baseValue > effectiveMax) baseValue else effectiveMax
    }

    @Synchronized
    fun clear() {
        currentFrameTime = -1L
        currentFrameMax = 0f
        lastFrameMax = 0f
    }
}
