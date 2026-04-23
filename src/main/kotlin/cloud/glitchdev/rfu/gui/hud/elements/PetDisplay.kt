package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.PetEvents.PetUpdateEventManager
import cloud.glitchdev.rfu.events.managers.PetEvents.registerPetUpdateEvent
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement

@HudElement
object PetDisplay : AbstractTextHudElement("petDisplay") {
    override val enabled: Boolean
        get() = OtherSettings.petDisplay && (super.enabled || PetUpdateEventManager.currentPet != null)

    private var currentPet : String? = null

    override fun onInitialize() {
        currentPet = PetUpdateEventManager.currentPet

        registerPetUpdateEvent { pet ->
            currentPet = pet
            updateState()
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()
        text.setText(currentPet ?: "${TextColor.LIGHT_RED}No pet")
    }
}
