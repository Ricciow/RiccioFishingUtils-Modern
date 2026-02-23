package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement

@HudElement
object PetDisplay : AbstractTextHudElement("petDisplay") {
    var currentPet : String? = null
        set(value) {
            field = value
            updateState()
        }

    override val enabled: Boolean
        get() = OtherSettings.petDisplay && (super.enabled || currentPet != null)

    override fun onUpdateState() {
        super.onUpdateState()
        text.setText(currentPet ?: "${TextColor.LIGHT_RED}No pet")
    }
}