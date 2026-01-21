package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.RiccioFishingUtils.minecraft
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.universal.UKeyboard

open class BaseWindow : WindowScreen(ElementaVersion.V10, drawDefaultBackground = true) {

    init {
        window.onKeyType { _, id ->
            if(id == UKeyboard.KEY_ESCAPE) {
                minecraft.send {
                    displayScreen(null)
                }
            }
        }
    }
}