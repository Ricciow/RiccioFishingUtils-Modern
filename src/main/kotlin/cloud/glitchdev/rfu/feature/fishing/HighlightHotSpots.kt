package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.config.categories.HotSpotSettings.hotspotBorderOpacity
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.cylinder
import java.awt.Color
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.tags.FluidTags
import net.minecraft.world.phys.Vec3

@RFUFeature
object HighlightHotSpots : Feature {

    override fun onInitialize() {
        registerRenderEvent { context ->
            if (!HotSpotSettings.highlightHotSpots) return@registerRenderEvent

            val world = mc.level ?: return@registerRenderEvent

            Render3D.draw(context) {
                for (hotspot in HotSpotEvents.getAllHotspots()) {
                    val rad = if (hotspot.radius > 0) hotspot.radius else continue
                    val surfaceY = findSurfaceY(hotspot.center, world, hotspot.liquid.isLava())
                    val renderPos = Vec3(hotspot.center.x, surfaceY + 0.1, hotspot.center.z)

                    val baseBorderColor = hotspot.color.darker()
                    val borderColor = Color(baseBorderColor.red, baseBorderColor.green, baseBorderColor.blue, hotspotBorderOpacity)

                    cylinder {
                        location = renderPos
                        radius = rad
                        height = -3.0f
                        slices = 32
                        color = hotspot.color
                        this.borderColor = borderColor
                        lineWidth = 3.0f
                        scaleWithDistance = true
                        topBorder = true
                        bottomBorder = false
                    }
                }
            }
        }
    }

    private fun findSurfaceY(pos: Vec3, world: ClientLevel, isLava: Boolean): Double {
        val fluidTag = if (isLava) FluidTags.LAVA else FluidTags.WATER
        val centerPos = BlockPos.containing(pos)

        for (dy in 5 downTo -10) {
            val current = centerPos.offset(0, dy, 0)
            val fluidState = world.getFluidState(current)
            if (fluidState.`is`(fluidTag) && !world.getFluidState(current.above()).`is`(fluidTag)) {
                return current.y + fluidState.getHeight(world, current).toDouble()
            }
        }
        return pos.y
    }
}
