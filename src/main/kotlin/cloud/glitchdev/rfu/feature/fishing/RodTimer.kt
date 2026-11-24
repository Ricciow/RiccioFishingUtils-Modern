package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.registerEntityRenderEvent
import cloud.glitchdev.rfu.events.managers.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.registerTickEvent
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

    override fun onInitialize(scope: EventScope) {
        scope.registerTickEvent(0, 2) { _ ->
            if(timer?.isRemoved ?: true) {
                timer = null
            }

            RodTimerDisplay.rodTime = if(timer?.name?.string == "!!!") {
                0f
            } else {
                timer?.name?.string?.toFloatOrNull() ?: -1f
            }

            RodTimerDisplay.maxTime = if (RodTimerDisplay.rodTime < 0) {
                RodTimerDisplay.DEFAULT_MAX_TIME
            } else {
                maxOf(RodTimerDisplay.maxTime, RodTimerDisplay.rodTime)
            }

            isHoldingRod = mc.player?.mainHandItem?.item?.descriptionId == "item.minecraft.fishing_rod"

            RodTimerDisplay.updateState()
        }

        scope.registerEntityRenderEvent { entity, _, event ->
            if (!GeneralFishing.rodTimerDisplay && !GeneralFishing.rodTimerSound && !GeneralFishing.muteNonRfuSounds) {
                return@registerEntityRenderEvent
            }
            if(!isHoldingRod) return@registerEntityRenderEvent

            if (timer != null && entity.id == timer?.id) {
                if (GeneralFishing.rodTimerDisplay) event.cancel()
                return@registerEntityRenderEvent
            }

            if (timer != null && !(timer?.isRemoved ?: true)) return@registerEntityRenderEvent
            if(entity !is ArmorStand) return@registerEntityRenderEvent
            if(!entity.hasCustomName()) return@registerEntityRenderEvent
            if(!entity.name.toUnformattedString().matches(timerRegex)) return@registerEntityRenderEvent

            timer = entity
            if (GeneralFishing.rodTimerDisplay) event.cancel()
        }

        scope.registerLocationEvent {
            timer = null
        }
    }
}
