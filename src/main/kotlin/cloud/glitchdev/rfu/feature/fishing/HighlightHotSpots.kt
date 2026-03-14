package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.managers.HotSpotEvents.registerHotSpotDetectedEvent
import cloud.glitchdev.rfu.events.managers.HotSpotEvents.registerHotSpotDisposeEvent
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.rendering.Render3D
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@RFUFeature
object HighlightHotSpots : Feature {
    private val hotspots = ConcurrentHashMap<UUID, Hotspot>()

    override fun onInitialize() {
        registerHotSpotDetectedEvent { hotspot ->
            hotspots[hotspot.uuid] = hotspot
        }

        registerHotSpotDisposeEvent { hotspot ->
            hotspots.remove(hotspot.uuid)
        }

        registerRenderEvent { context ->
            if (!HotSpotSettings.highlightHotSpots) return@registerRenderEvent

            val world = mc.level ?: return@registerRenderEvent

            for (hotspot in hotspots.values) {
                val radius = if (hotspot.radius > 0) hotspot.radius else continue
                val surfaceY = findSurfaceY(hotspot.center, world, hotspot.lava)
                val renderPos = Vec3(hotspot.center.x, surfaceY + 0.01, hotspot.center.z)

                Render3D.renderDisk(
                    renderPos,
                    radius,
                    -3.0f,
                    hotspot.color,
                    context,
                    borderColor = hotspot.color.darker(),
                    lineWidth = 3.0f
                )
            }
        }
    }

    private fun findSurfaceY(pos: Vec3, world: ClientLevel, isLava: Boolean): Double {
        val blockType = if (isLava) Blocks.LAVA else Blocks.WATER
        val centerPos = net.minecraft.core.BlockPos.containing(pos)

        for (dy in 5 downTo -10) {
            val current = centerPos.offset(0, dy, 0)
            if (world.getBlockState(current).`is`(blockType) && !world.getBlockState(current.above()).`is`(blockType)) {
                return current.y + 1.0
            }
        }
        return pos.y
    }
}
