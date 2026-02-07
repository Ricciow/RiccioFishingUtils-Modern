package cloud.glitchdev.rfu.manager.hud

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.utils.JsonFile

@AutoRegister
object HudManager : RegisteredEvent {
    val hudFile = JsonFile(
        filename = "hud.json",
        type = HudConfig::class.java,
        defaultFactory = { HudConfig() }
    )

    val hudData = hudFile.data

    override fun register() {
        registerJoinEvent {
            hudFile.save()
        }

        registerShutdownEvent(1000) {
            hudFile.save()
        }
    }

    fun getElementConfig(element : AbstractHudElement) : HudConfig.HudElement {
        return hudData.getOrAdd(element.id, element.defaultX, element.defaultY, element.scale)
    }

    fun updateElementConfig(element: AbstractHudElement) {
        hudData.update(element.id, element.currentX, element.currentY, element.scale)
    }
}