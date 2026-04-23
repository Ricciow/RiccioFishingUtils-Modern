package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.constants.Bait
import cloud.glitchdev.rfu.constants.HotspotType
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.UIScheme.decreaseOpacity
import cloud.glitchdev.rfu.gui.UIScheme.increaseOpacity
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.line
import net.minecraft.world.phys.Vec3

@RFUFeature
object HotspotPointer : Feature {
    var lastHotspot: Hotspot? = null

    override fun onInitialize() {
        registerSeaCreatureCatchEvent { _, _, hotspot, _, _ ->
            lastHotspot = hotspot
        }

        registerLocationEvent {
            lastHotspot = null
        }

        registerRenderEvent { context ->
            if (!HotSpotSettings.hotspotPointer) return@registerRenderEvent
            if (lastHotspot == null) return@registerRenderEvent

            val playerPos = mc.player?.position() ?: return@registerRenderEvent

            val hotspots = HotSpotEvents.getAllHotspots().filter { it.type != HotspotType.UNKNOWN }
            if (hotspots.isEmpty()) return@registerRenderEvent

            val priorityList = HotSpotSettings.hotspotPointerPriority.toList()
            val bestHotspot = hotspots.minWithOrNull(
                compareBy<Hotspot> { 
                    priorityList.indexOf(it.type) 
                }.thenBy { it.center.distanceTo(playerPos) }
            ) ?: return@registerRenderEvent

            val isNearBest = playerPos.distanceTo(bestHotspot.center) < 10.0
            val wasLastFished = lastHotspot?.uuid == bestHotspot.uuid

            if (isNearBest || wasLastFished) return@registerRenderEvent

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
