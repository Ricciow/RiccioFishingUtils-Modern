package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.TrophyFishing
import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.events.managers.BlockInteractEvents.registerBlockInteractEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.cube
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.minecraft.world.phys.Vec3
import java.awt.Color

@RFUFeature
object SulphurSkitterRange : Feature {
    enum class SulphurSkitterMode {
        AUTO, MANUAL;

        override fun toString(): String = name.lowercase().replaceFirstChar { it.uppercase() }
    }

    private const val SEARCH_RADIUS = 30
    private val automaticSponges = mutableSetOf<BlockPos>()
    private val selectedSponges = mutableSetOf<BlockPos>()
    private val faceColor = Color(255, 255, 0, 20)
    private val outlineColor = Color(255, 255, 0, 200)

    private val enabled: Boolean
        get() = TrophyFishing.sulphurSkitterRange && World.island == FishingIslands.ISLE &&
            FishingSession.isTrophyFishing

    override fun onInitialize() {
        registerLocationEvent { clear() }
        registerDisconnectEvent { clear() }

        registerBlockInteractEvent { pos ->
            if (!enabled || TrophyFishing.sulphurSkitterMode != SulphurSkitterMode.MANUAL) return@registerBlockInteractEvent
            if (mc.level?.getBlockState(pos)?.`is`(Blocks.SPONGE) != true) return@registerBlockInteractEvent
            if (!selectedSponges.add(pos.immutable())) selectedSponges.remove(pos)
        }

        registerTickEvent(interval = 20) {
            if (!enabled) {
                clear()
                return@registerTickEvent
            }
            val world = mc.level ?: return@registerTickEvent
            selectedSponges.removeAll { !world.chunkSource.hasChunk(it.x shr 4, it.z shr 4) || !world.getBlockState(it).`is`(Blocks.SPONGE) }
            automaticSponges.clear()
            if (TrophyFishing.sulphurSkitterMode != SulphurSkitterMode.AUTO) return@registerTickEvent
            val player = mc.player ?: return@registerTickEvent
            val center = player.blockPosition()
            val playerPos = player.position()
            for (cx in ((center.x - SEARCH_RADIUS) shr 4)..((center.x + SEARCH_RADIUS) shr 4)) {
                for (cz in ((center.z - SEARCH_RADIUS) shr 4)..((center.z + SEARCH_RADIUS) shr 4)) {
                    val chunk = world.chunkSource.getChunk(cx, cz, ChunkStatus.FULL, false) ?: continue
                    for ((index, section) in chunk.sections.withIndex()) {
                        val baseY = chunk.getSectionYFromSectionIndex(index) shl 4
                        if (baseY + 15 < center.y - SEARCH_RADIUS || baseY > center.y + SEARCH_RADIUS) continue
                        if (!section.maybeHas { it.`is`(Blocks.SPONGE) }) continue
                        for (x in 0..15) for (y in 0..15) for (z in 0..15) {
                            if (section.getBlockState(x, y, z).`is`(Blocks.SPONGE)) {
                                val pos = BlockPos((cx shl 4) + x, baseY + y, (cz shl 4) + z)
                                if (pos.distToCenterSqr(playerPos) <= SEARCH_RADIUS * SEARCH_RADIUS) {
                                    automaticSponges.add(pos)
                                }
                            }
                        }
                    }
                }
            }
        }

        registerRenderEvent { context ->
            if (!enabled) return@registerRenderEvent
            val world = mc.level ?: return@registerRenderEvent
            val playerPos = mc.player?.position() ?: return@registerRenderEvent
            val sponges = if (TrophyFishing.sulphurSkitterMode == SulphurSkitterMode.AUTO) automaticSponges else selectedSponges
            Render3D.draw(context) {
                for (pos in sponges) {
                    if (pos.distToCenterSqr(playerPos) > SEARCH_RADIUS * SEARCH_RADIUS) continue
                    if (!world.chunkSource.hasChunk(pos.x shr 4, pos.z shr 4) || !world.getBlockState(pos).`is`(Blocks.SPONGE)) continue
                    cube {
                        position = Vec3.atCenterOf(pos)
                        size = 9f
                        filled = true
                        color = faceColor
                        borderColor = outlineColor
                    }
                }
            }
        }
    }

    private fun clear() {
        automaticSponges.clear()
        selectedSponges.clear()
    }
}
