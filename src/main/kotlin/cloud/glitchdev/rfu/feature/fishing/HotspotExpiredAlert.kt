package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.HotSpotEvents.registerHotSpotDisposeEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Sounds
import java.util.UUID

@RFUFeature
object HotspotExpiredAlert : Feature {
    private var currentHotspotId: UUID? = null

    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            if (!HotSpotSettings.hotspotExpired.enabled) return@registerTickEvent

            val bobber = mc.player?.fishing
            if (bobber != null) {
                val hotspot = HotSpotEvents.getHotspotAt(bobber.position())
                if (hotspot != null) {
                    currentHotspotId = hotspot.uuid
                }
            }

            if (!FishingSession.isFishing) {
                currentHotspotId = null
            }
        }

        registerHotSpotDisposeEvent { hotspot ->
            if (!HotSpotSettings.hotspotExpired.enabled) return@registerHotSpotDisposeEvent
            if (hotspot.uuid != currentHotspotId) return@registerHotSpotDisposeEvent

            currentHotspotId = null
            HotSpotSettings.hotspotExpired.show()
            if (HotSpotSettings.hotspotExpired.sound) {
                Sounds.playSound("rfu:hotspot_expired", 1f, HotSpotSettings.hotspotExpired.volume)
            }
        }
    }
}
