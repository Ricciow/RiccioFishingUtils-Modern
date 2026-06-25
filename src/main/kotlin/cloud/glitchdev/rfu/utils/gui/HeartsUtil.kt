package cloud.glitchdev.rfu.utils.gui

object HeartsUtil {
    @JvmStatic
    var forceHardcoreHearts: Boolean = false

    @JvmStatic
    fun enableHardcoreHearts() {
        forceHardcoreHearts = true
    }

    @JvmStatic
    fun disableHardcoreHearts() {
        forceHardcoreHearts = false
    }

    @JvmStatic
    fun toggleHardcoreHearts() {
        forceHardcoreHearts = !forceHardcoreHearts
    }
}
