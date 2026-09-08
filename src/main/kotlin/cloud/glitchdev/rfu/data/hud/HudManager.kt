package cloud.glitchdev.rfu.data.hud

import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.utils.JsonFile

object HudManager {
    val hudFile = JsonFile(
        filename = "hud.json",
        type = HudConfig::class.java,
        defaultFactory = { HudConfig() }
    )

    val hudData get() = hudFile.data

    fun hasElementConfig(id: String): Boolean {
        return hudData.hudElements.any { it.id == id }
    }

    fun getElementConfig(element: AbstractHudElement): HudConfig.HudElement? {
        return hudData.hudElements.find { it.id == element.id }
    }

    fun updateElementConfig(element: AbstractHudElement) {
        hudData.update(element.id, element.currentX, element.currentY, element.scale)
    }

    fun saveResolution(screenWidth: Float, screenHeight: Float) {
        if (screenWidth > 0f && screenHeight > 0f) {
            hudData.screenWidth = screenWidth
            hudData.screenHeight = screenHeight
        }
    }

    fun resetToDefaults(screenWidth: Float, screenHeight: Float, elements: List<AbstractHudElement>) {
        hudData.hudElements.clear()
        saveResolution(screenWidth, screenHeight)
        for (element in elements) {
            val calculated = DefaultHudManager.calculateDefaultPosition(element, screenWidth, screenHeight)
            element.currentX = calculated.x
            element.currentY = calculated.y
            element.scale = calculated.scale
            hudData.update(element.id, calculated.x, calculated.y, calculated.scale)
            element.updateState()
        }
        hudData.hasInitializedDefaults = true
        hudFile.save()
    }
}