package cloud.glitchdev.rfu.gui.components

interface Colorable {
    fun refreshColors()
}

inline fun <T : Colorable> T.colors(block: T.() -> Unit): T {
    this.block()
    this.refreshColors()
    return this
}
