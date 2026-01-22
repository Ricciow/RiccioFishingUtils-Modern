package cloud.glitchdev.rfu.manager.hud

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.gui.components.hud.AbstractHudElement
import cloud.glitchdev.rfu.utils.JsonFile
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

@AutoRegister
object HudManager : RegisteredEvent {
    val hudFile = JsonFile(
        filename = "hud.json",
        type = HudConfig::class.java,
        defaultFactory = { HudConfig() }
    )

    val hudData = hudFile.data

    override fun register() {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            hudFile.save()
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            hudFile.save()
        }
    }

    fun getElementConfig(element : AbstractHudElement) : HudConfig.HudElement {
        return hudData.getOrAdd(element.id, element.defaultX, element.defaultY, element.enabled, element.scale)
    }

    fun updateElementConfig(element: AbstractHudElement) {
        hudData.update(element.id, element.currentX, element.currentY, element.enabled, element.scale)
    }
}