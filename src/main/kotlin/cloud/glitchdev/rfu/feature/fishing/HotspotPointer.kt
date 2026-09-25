package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.constants.fishing.HotspotType
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.UIScheme.decreaseOpacity
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.line
import net.minecraft.world.phys.Vec3
import java.util.concurrent.atomic.AtomicReference

@RFUFeature
object HotspotPointer : Feature {
    private val bestHotspotRef = AtomicReference<Hotspot?>(null)

    override fun onInitialize() {
        registerLocationEvent {
            bestHotspotRef.set(null)
        }

        registerTickEvent(interval = 10) {
            if (!HotSpotSettings.hotspotPointer || !FishingSession.isHotspotFishing) {
                bestHotspotRef.set(null)
                return@registerTickEvent
            }

            val playerPos = mc.player?.position() ?: run {
                bestHotspotRef.set(null)
                return@registerTickEvent
            }

            val currentHotspots = HotSpotEvents.getAllHotspots().filter { it.type != HotspotType.UNKNOWN }
            if (currentHotspots.isEmpty()) {
                bestHotspotRef.set(null)
                return@registerTickEvent
            }

            val priorityList = HotSpotSettings.hotspotPointerPriority.toList()
            val currentHotspot = getCurrentActiveHotspot(playerPos, currentHotspots)

            val candidates = if (currentHotspot != null) {
                val currentPriority = getPriorityIndex(currentHotspot.type, priorityList)
                currentHotspots.filter { getPriorityIndex(it.type, priorityList) < currentPriority }
            } else {
                currentHotspots
            }

            if (candidates.isEmpty()) {
                bestHotspotRef.set(null)
                return@registerTickEvent
            }

            val calculatedBest = candidates.minWithOrNull(
                compareBy<Hotspot> {
                    getPriorityIndex(it.type, priorityList)
                }.thenBy { it.center.distanceTo(playerPos) }
            )

            bestHotspotRef.set(calculatedBest)
        }

        registerRenderEvent { context ->
            if (!HotSpotSettings.hotspotPointer || !FishingSession.isHotspotFishing) return@registerRenderEvent

            val bestHotspot = bestHotspotRef.get() ?: return@registerRenderEvent
            if (!HotSpotEvents.getAllHotspots().contains(bestHotspot)) return@registerRenderEvent

            val playerPos = mc.player?.position() ?: return@registerRenderEvent
            if (playerPos.distanceTo(bestHotspot.center) < 10.0) return@registerRenderEvent

            Render3D.draw(context) {
                line {
                    startPosition = camera
                    position = bestHotspot.center
                    color = bestHotspot.color.decreaseOpacity(255)
                    lineWidth = 3.0f
                }
            }
        }
    }

    private fun getPriorityIndex(type: HotspotType, priorityList: List<HotspotType>): Int {
        val index = priorityList.indexOf(type)
        return if (index == -1) Int.MAX_VALUE else index
    }

    private fun getCurrentActiveHotspot(playerPos: Vec3, activeHotspots: List<Hotspot>): Hotspot? {
        val bobber = mc.player?.fishing
        if (bobber != null) {
            val atBobber = HotSpotEvents.getHotspotAt(bobber.position())
            if (atBobber != null && atBobber.type != HotspotType.UNKNOWN && activeHotspots.contains(atBobber)) {
                return atBobber
            }
        }

        val atPlayer = HotSpotEvents.getHotspotAt(playerPos)
        if (atPlayer != null && atPlayer.type != HotspotType.UNKNOWN && activeHotspots.contains(atPlayer)) {
            return atPlayer
        }

        return activeHotspots.find { playerPos.distanceTo(it.center) < 10.0 }
    }
}