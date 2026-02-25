package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.EntityRenderEvents.registerEntityRenderEvent
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
    var timer : ArmorStand? = null
    private var isHoldingRod = false

    override fun onInitialize() {
        registerTickEvent(0, 2) { _ ->
            if(timer?.isRemoved ?: true) {
                timer = null
            }

            RodTimerDisplay.rodTime = if(timer?.name?.string == "!!!") {
                0f
            } else {
                timer?.name?.string?.toFloatOrNull() ?: -1f
            }

            isHoldingRod = mc.player?.mainHandItem?.item?.descriptionId == "item.minecraft.fishing_rod"

            RodTimerDisplay.updateState()
        }

        registerEntityRenderEvent { entity, event ->
            if(!GeneralFishing.rodTimerDisplay) return@registerEntityRenderEvent
            if(!isHoldingRod) return@registerEntityRenderEvent

            if (timer != null && entity.id == timer?.id) {
                event.cancel()
                return@registerEntityRenderEvent
            }

            if (timer != null) return@registerEntityRenderEvent
            if(entity !is ArmorStand) return@registerEntityRenderEvent
            if(!entity.hasCustomName()) return@registerEntityRenderEvent
            if(!entity.name.toUnformattedString().matches(timerRegex)) return@registerEntityRenderEvent

            timer = entity
            event.cancel()
        }
    }
}