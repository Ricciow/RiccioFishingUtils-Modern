package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.RodTimerDisplay
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.world.entity.decoration.ArmorStand

@RFUFeature
object RodTimer : Feature {
    val timerRegex = """!!!|\d\.\d""".toExactRegex()

    override fun onInitialize() {
        registerTickEvent(0, 2) { client ->
            val world = client.level ?: return@registerTickEvent

            val entity = world.entitiesForRendering().find { entity ->
                if(entity !is ArmorStand) return@find false
                if (!entity.hasCustomName()) return@find false
                return@find entity.name.toUnformattedString().matches(timerRegex)
            } as? ArmorStand

            RodTimerDisplay.rodTime = entity?.name?.string?.toFloatOrNull() ?: -1f
            RodTimerDisplay.updateState()
        }
    }
}