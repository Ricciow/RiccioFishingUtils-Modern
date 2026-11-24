package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.EventScopes
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.universal.UKeyboard

abstract class BaseWindow(drawDefaultBackground : Boolean = false) : WindowScreen(ElementaVersion.V11, drawDefaultBackground = drawDefaultBackground) {
    private val windowScopes = EventScopes()

    init {
        useElementaRenderer = true

        window.onKeyType { _, id ->
            if(id == UKeyboard.KEY_ESCAPE) {
                closeScreen()
            }
        }
    }

    open fun onWindowClose() {}
    open fun onOpenWindow() {}

    protected open fun initializeWindow(scope: EventScope) {}

    final override fun initScreen(width: Int, height: Int) {
        super.initScreen(width, height)
        windowScopes.load(::initializeWindow)
    }

    final override fun onScreenClose() {
        try {
            windowScopes.close()
        } finally {
            super.onScreenClose()
        }
    }

    fun closeScreen() {
        mc.schedule {
            displayScreen(null)
            onWindowClose()
        }
    }
}
