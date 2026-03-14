package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.managers.ParticleEvents.registerParticleEvent
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.rendering.Render3D
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.Display.TextDisplay
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import java.awt.Color
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

@RFUFeature
object HighlightHotSpots : Feature {
    private val hotspots = ConcurrentHashMap<UUID, Hotspot>()

    override fun onInitialize() {
        registerTickEvent(interval = 20) { client ->
            if (!HotSpotSettings.highlightHotSpots) {
                hotspots.clear()
                return@registerTickEvent
            }

            val world = client.level ?: return@registerTickEvent

            val entities = world.entitiesForRendering()
            val seenUuids = mutableSetOf<UUID>()

            entities.forEach { entity ->
                val name = when (entity) {
                    is ArmorStand -> if (entity.hasCustomName()) entity.customName?.toUnformattedString() else null
                    is TextDisplay -> entity.text.toUnformattedString()
                    else -> null
                } ?: ""

                if (name.contains("HOTSPOT", ignoreCase = true)) {
                    val uuid = entity.uuid
                    seenUuids.add(uuid)
                    
                    if (!hotspots.containsKey(uuid)) {
                        val pos = entity.position()
                        val buff = findBuffNearby(pos, world)
                        val color = getColorForBuff(buff)
                        val isLava = checkIsLava(pos, world)
                        hotspots[uuid] = Hotspot(uuid, pos, buff, 0f, color, isLava)
                    }
                }
            }

            hotspots.keys.removeIf { !seenUuids.contains(it) }
        }

        registerParticleEvent { packet, cancelable ->
            if (!HotSpotSettings.highlightHotSpots) return@registerParticleEvent
            if (!isAllowedParticle(packet.particle)) return@registerParticleEvent

            val pos = Vec3(packet.x, packet.y, packet.z)
            val closestHotspot = hotspots.values.minByOrNull { it.center.distanceTo(pos) } ?: return@registerParticleEvent

            if (abs(pos.y - closestHotspot.center.y) > 6.0) return@registerParticleEvent

            val horizontalDistance = Vec3(pos.x, 0.0, pos.z).distanceTo(Vec3(closestHotspot.center.x, 0.0, closestHotspot.center.z))

            if (horizontalDistance < 6.0) {
                closestHotspot.addParticleDistance(horizontalDistance)
                cancelable.cancel()
            }
        }

        registerRenderEvent { context ->
            if (!HotSpotSettings.highlightHotSpots) return@registerRenderEvent

            val world = mc.level ?: return@registerRenderEvent

            for (hotspot in hotspots.values) {
                val radius = if (hotspot.radius > 0) hotspot.radius else 4.0f
                val surfaceY = findSurfaceY(hotspot.center, world, hotspot.lava)
                val renderPos = Vec3(hotspot.center.x, surfaceY + 0.05, hotspot.center.z)

                Render3D.renderDisk(
                    renderPos,
                    radius,
                    -1.0f,
                    hotspot.color,
                    context,
                    borderColor = hotspot.color.darker(),
                    lineWidth = 3.0f
                )
            }
        }
    }

    private fun isAllowedParticle(particle : ParticleOptions) : Boolean {
        return when(particle) {
            ParticleTypes.FISHING -> false
            ParticleTypes.SPLASH -> false
            ParticleTypes.BUBBLE -> false
            ParticleTypes.WITCH -> false
            else -> true
        }
    }

    private fun findBuffNearby(pos: Vec3, world: net.minecraft.client.multiplayer.ClientLevel): String {
        val searchBox = net.minecraft.world.phys.AABB(
            pos.x - 0.5, pos.y - 4.0, pos.z - 0.5, 
            pos.x + 0.5, pos.y + 4.0, pos.z + 0.5
        )
        
        val entities = world.getEntitiesOfClass(ArmorStand::class.java, searchBox).toList()
        val textDisplays = world.getEntitiesOfClass(TextDisplay::class.java, searchBox).toList()
        
        for (entity in entities + textDisplays) {
            val name = when (entity) {
                is ArmorStand -> if (entity.hasCustomName()) entity.customName?.toUnformattedString() else null
                is TextDisplay -> entity.text.toUnformattedString()
                else -> null
            } ?: ""
            
            if (name.contains("Chance", ignoreCase = true) || 
                name.contains("Speed", ignoreCase = true) ||
                name.contains("Double Hook", ignoreCase = true)) {
                return name
            }
        }
        return ""
    }

    private fun getColorForBuff(buff: String): Color {
        return when {
            buff.contains("Treasure", ignoreCase = true) -> Color(255, 170, 0, 100) // Orange
            buff.contains("Speed", ignoreCase = true) -> Color(85, 255, 85, 100) // Green
            buff.contains("Sea Creature", ignoreCase = true) -> Color(255, 85, 255, 100) // Pink
            buff.contains("Double Hook", ignoreCase = true) -> Color(85, 255, 255, 100) // Cyan
            else -> Color(255, 255, 255, 100) // White default
        }
    }

    private fun checkIsLava(pos: Vec3, world: net.minecraft.client.multiplayer.ClientLevel): Boolean {
        for (dx in -1..1) {
            for (dz in -1..1) {
                for (dy in -4..1) {
                    val blockPos = net.minecraft.core.BlockPos.containing(pos.x + dx, pos.y + dy, pos.z + dz)
                    if (world.getBlockState(blockPos).`is`(Blocks.LAVA)) return true
                }
            }
        }
        return false
    }

    private fun findSurfaceY(pos: Vec3, world: net.minecraft.client.multiplayer.ClientLevel, isLava: Boolean): Double {
        val blockType = if (isLava) Blocks.LAVA else Blocks.WATER
        var highestY = -64.0
        
        for (dx in -1..1) {
            for (dz in -1..1) {
                for (dy in 5 downTo -10) {
                    val blockPos = net.minecraft.core.BlockPos.containing(pos.x + dx, pos.y + dy, pos.z + dz)
                    val state = world.getBlockState(blockPos)
                    if (state.`is`(blockType)) {
                        val above = world.getBlockState(blockPos.above())
                        if (!above.`is`(blockType)) {
                            val currentSurfaceY = blockPos.y + 1.0
                            if (currentSurfaceY > highestY) {
                                highestY = currentSurfaceY
                            }
                            break
                        }
                    }
                }
            }
        }
        return if (highestY > -64.0) highestY else pos.y
    }
}
