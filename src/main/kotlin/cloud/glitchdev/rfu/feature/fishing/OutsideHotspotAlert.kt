package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World
import net.minecraft.world.entity.projectile.FishingHook

@RFUFeature
object OutsideHotspotAlert : Feature {
    private var trackedBobber: FishingHook? = null
    private var wasOutsideHotspot = false

    override fun onInitialize() {
        registerTickEvent(interval = 5) {
            val bobber = mc.player?.fishing
            if (!HotSpotSettings.outsideHotspot.enabled || !World.isInSkyblock ||
                bobber == null || bobber.isRemoved
            ) {
                trackedBobber = null
                wasOutsideHotspot = false
                return@registerTickEvent
            }
            if (!bobber.isInWater && !bobber.isInLava) return@registerTickEvent

            val isOutsideHotspot = HotSpotEvents.getHotspotAt(bobber.position()) == null
            if (isOutsideHotspot && (bobber !== trackedBobber || !wasOutsideHotspot)) {
                HotSpotSettings.outsideHotspot.show()
            }
            trackedBobber = bobber
            wasOutsideHotspot = isOutsideHotspot
        }
    }
}
