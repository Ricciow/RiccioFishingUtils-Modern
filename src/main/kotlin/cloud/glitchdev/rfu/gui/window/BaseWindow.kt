package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.universal.UKeyboard

abstract class BaseWindow(drawDefaultBackground : Boolean = false) : WindowScreen(ElementaVersion.V10, drawDefaultBackground = drawDefaultBackground) {
    init {
        window.onKeyType { _, id ->
            if(id == UKeyboard.KEY_ESCAPE) {
                onWindowClose()
            }

            mc.schedule {
                displayScreen(null)
            }
        }
    }

     open fun onWindowClose() {}
}