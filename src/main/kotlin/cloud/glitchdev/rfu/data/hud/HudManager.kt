package cloud.glitchdev.rfu.data.hud

import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.utils.JsonFile

object HudManager {
    val hudFile = JsonFile(
        filename = "hud.json",
        type = HudConfig::class.java,
        defaultFactory = { HudConfig() }
    )

    val hudData = hudFile.data

    fun getElementConfig(element : AbstractHudElement) : HudConfig.HudElement {
        return hudData.getOrAdd(element.id, element.defaultX, element.defaultY, element.scale)
    }

    fun updateElementConfig(element: AbstractHudElement) {
        hudData.update(element.id, element.currentX, element.currentY, element.scale)
    }
}