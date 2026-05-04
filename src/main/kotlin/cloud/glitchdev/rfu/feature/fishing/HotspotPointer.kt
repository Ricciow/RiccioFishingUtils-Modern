package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.constants.HotspotType
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.managers.HotSpotEvents.registerHotSpotChangedEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
// Make sure to import your registerTickEvent here
// import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.UIScheme.decreaseOpacity
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.line
import java.util.concurrent.atomic.AtomicReference

@RFUFeature
object HotspotPointer : Feature {
    val lastHotspot
        get() = FishingSession.lastHotspot
    private val bestHotspotRef = AtomicReference<Hotspot?>(null)
    private var currentHotspots: List<Hotspot> = emptyList()
    private var wasNearBest = false
    private var hasFishedSinceArriving = false

    override fun onInitialize() {
        registerSeaCreatureCatchEvent { _, _, _, _, _ ->
            hasFishedSinceArriving = true
        }

        registerLocationEvent {
            hasFishedSinceArriving = false
        }

        registerHotSpotChangedEvent { hotspots ->
            currentHotspots = hotspots.filter { it.type != HotspotType.UNKNOWN }
        }

        registerTickEvent(interval = 10) {
            val playerPos = mc.player?.position() ?: return@registerTickEvent

            if (currentHotspots.isEmpty()) {
                bestHotspotRef.set(null)
                return@registerTickEvent
            }

            val priorityList = HotSpotSettings.hotspotPointerPriority.toList()

            val calculatedBest = currentHotspots.minWithOrNull(
                compareBy<Hotspot> {
                    priorityList.indexOf(it.type)
                }.thenBy { it.center.distanceTo(playerPos) }
            )

            bestHotspotRef.set(calculatedBest)
        }

        registerRenderEvent { context ->
            if (!HotSpotSettings.hotspotPointer) return@registerRenderEvent
            if (lastHotspot == null) return@registerRenderEvent

            val bestHotspot = bestHotspotRef.get() ?: return@registerRenderEvent
            val playerPos = mc.player?.position() ?: return@registerRenderEvent

            val isNearBest = playerPos.distanceTo(bestHotspot.center) < 10.0
            if (!wasNearBest && isNearBest) {
                hasFishedSinceArriving = false
            }
            wasNearBest = isNearBest

            val wasLastFished = lastHotspot?.uuid == bestHotspot.uuid
            if (isNearBest && (!hasFishedSinceArriving || wasLastFished)) {
                return@registerRenderEvent
            }

            Render3D.draw(context) {
                line {
                    startLocation = camera
                    location = bestHotspot.center
                    color = bestHotspot.color.decreaseOpacity(255)
                    lineWidth = 3.0f
                }
            }
        }
    }
}