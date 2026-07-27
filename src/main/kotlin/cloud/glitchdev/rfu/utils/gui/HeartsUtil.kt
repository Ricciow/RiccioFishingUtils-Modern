package cloud.glitchdev.rfu.utils.gui

object HeartsUtil {
    var forceHardcoreHearts: Boolean = false
        private set

    fun enableHardcoreHearts() {
        forceHardcoreHearts = true
    }

    fun disableHardcoreHearts() {
        forceHardcoreHearts = false
    }

    fun toggleHardcoreHearts() {
        forceHardcoreHearts = !forceHardcoreHearts
    }
}
