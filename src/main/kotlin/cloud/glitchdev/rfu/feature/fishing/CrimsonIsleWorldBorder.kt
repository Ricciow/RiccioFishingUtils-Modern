package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.events.managers.WorldBorderRenderEvents.WorldBorderRenderContext
import cloud.glitchdev.rfu.events.managers.WorldBorderRenderEvents.registerWorldBorderRenderEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.level.border.WorldBorder
import kotlin.math.abs
import kotlin.math.expm1

@RFUFeature
object CrimsonIsleWorldBorder : Feature {
    private const val BORDER_Z = -337.0
    private const val FADE_START_DISTANCE = 60.0
    private const val FADE_END_DISTANCE = 30.0
    private const val FADE_STEEPNESS = 4.0

    override fun onInitialize() {
        registerWorldBorderRenderEvent { context ->
            if (!LavaFishing.showCrimsonIsleWorldBorder || World.island != FishingIslands.ISLE) return@registerWorldBorderRenderEvent
            val player = mc.player ?: return@registerWorldBorderRenderEvent
            if (mc.level == null) return@registerWorldBorderRenderEvent

            val distance = abs(player.getPosition(context.partialTick).z - BORDER_Z)
            if (distance >= FADE_START_DISTANCE) return@registerWorldBorderRenderEvent

            val state = context.state
            state.minX = -WorldBorder.MAX_CENTER_COORDINATE
            state.maxX = WorldBorder.MAX_CENTER_COORDINATE
            state.minZ = BORDER_Z
            state.maxZ = WorldBorder.MAX_CENTER_COORDINATE
            state.tint = BorderStatus.STATIONARY.color
            val proximity = ((FADE_START_DISTANCE - distance) / (FADE_START_DISTANCE - FADE_END_DISTANCE)).coerceIn(0.0, 1.0)
            state.alpha = expm1(proximity * FADE_STEEPNESS) / expm1(FADE_STEEPNESS)
        }
    }
}
