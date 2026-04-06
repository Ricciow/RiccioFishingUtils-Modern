package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.data.fishing.HotspotCache
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ParticleEvents.registerParticleEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.utils.World
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.Display.TextDisplay
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import java.awt.Color
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

@AutoRegister
object HotSpotEvents : RegisteredEvent {
    private val hotspots = ConcurrentHashMap<UUID, Hotspot>()
    private val virtualUuids = mutableSetOf<UUID>()

    override fun register() {
        HotspotCache.getCachedEntries(null)

        registerTickEvent(interval = 20) { client ->
            val world = client.level ?: return@registerTickEvent
            val entities = world.entitiesForRendering()
            val seenUuids = mutableSetOf<UUID>()
            val now = System.currentTimeMillis()

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
                        val blockPos = net.minecraft.core.BlockPos.containing(pos.x, pos.y, pos.z)

                        // Check if there is a virtual hotspot at this location and remove it
                        val virtualUuid = UUID.nameUUIDFromBytes("virtual_${blockPos}".toByteArray())
                        if (hotspots.containsKey(virtualUuid)) {
                            val virtualHotspot = hotspots.remove(virtualUuid)
                            virtualUuids.remove(virtualUuid)
                            if (virtualHotspot != null && virtualHotspot.isNotified) {
                                HotSpotDisposedEventManager.runTasks(virtualHotspot)
                            }
                        }

                        val buff = findBuffNearby(pos, world)
                        val color = getColorForBuff(buff)
                        val liquid = getLiquidType(pos, world)
                        val hotspot = Hotspot(uuid, pos, buff, 0f, color, liquid)
                        hotspot.isNotified = true
                        hotspots[uuid] = hotspot
                        HotSpotDetectedEventManager.runTasks(hotspot)
                    } else {
                        val hotspot = hotspots[uuid]!!
                        virtualUuids.remove(uuid)
                        if (!hotspot.isNotified) {
                            hotspot.isNotified = true
                            HotSpotDetectedEventManager.runTasks(hotspot)
                        }
                    }
                }
            }

            val iterator = hotspots.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val uuid = entry.key
                val hotspot = entry.value

                val isSeen = seenUuids.contains(uuid)

                if (!isSeen) {
                    virtualUuids.add(uuid)
                    // Virtual hotspots expire after 1 second of no updates
                    if (now - hotspot.lastUpdate > 1000) {
                        iterator.remove()
                        virtualUuids.remove(uuid)
                        if (hotspot.isNotified) {
                            HotSpotDisposedEventManager.runTasks(hotspot)
                        }
                    }
                }
            }
        }

        registerParticleEvent { packet, cancelable ->
            val particleOptions = packet.particle
            val particleType = particleOptions.type
            val isDust = particleType == ParticleTypes.DUST
            val isSmoke = particleType == ParticleTypes.SMOKE

            if (!isDust && !isSmoke) return@registerParticleEvent

            if (isDust && particleOptions is DustParticleOptions) {
                val colorVec = particleOptions.color

                val red = (colorVec.x * 255).toInt()
                val green = (colorVec.y * 255).toInt()
                val blue = (colorVec.z * 255).toInt()

                if(red != 255 || green != 105 || blue != 180) return@registerParticleEvent
            }
            val pos = Vec3(packet.x, packet.y, packet.z)

            var closestHotspot = hotspots.values
                .filter { hotspot ->
                    if (isSmoke) hotspot.liquid == LiquidTypes.LAVA
                    else hotspot.liquid == LiquidTypes.WATER
                }
                .minByOrNull { it.center.distanceTo(pos) }

            if (closestHotspot == null || abs(pos.y - closestHotspot.center.y) > 6.0 || Vec3(pos.x, 0.0, pos.z).distanceTo(Vec3(closestHotspot.center.x, 0.0, closestHotspot.center.z)) > 6.0) {
                val cachedEntry = HotspotCache.getCachedEntries(World.island).find { (blockPos, data) ->
                    val center = Vec3(blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5)

                    val liquidMatches = if (isSmoke) data.liquid == LiquidTypes.LAVA else data.liquid == LiquidTypes.WATER
                    liquidMatches && abs(pos.y - center.y) <= 6.0 && Vec3(pos.x, 0.0, pos.z).distanceTo(Vec3(center.x, 0.0, center.z)) <= 6.0
                }

                if (cachedEntry != null) {
                    val (blockPos, data) = cachedEntry
                    val center = Vec3(blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5)
                    val uuid = UUID.nameUUIDFromBytes("virtual_${blockPos}".toByteArray())

                    val now = System.currentTimeMillis()
                    val buff = if (now - data.lastMetadataUpdate < 30000) data.sessionBuff else ""
                    val color = if (buff.isNotEmpty()) getColorForBuff(buff) else Color.WHITE

                    closestHotspot = hotspots.getOrPut(uuid) {
                        virtualUuids.add(uuid)
                        Hotspot(uuid, center, buff, 0f, color, data.liquid)
                    }
                }
            }

            if (closestHotspot == null) return@registerParticleEvent
            if (abs(pos.y - closestHotspot.center.y) > 6.0) return@registerParticleEvent

            val horizontalDistance = Vec3(pos.x, 0.0, pos.z).distanceTo(Vec3(closestHotspot.center.x, 0.0, closestHotspot.center.z))

            if (horizontalDistance < 6.0) {
                closestHotspot.addParticleDistance(horizontalDistance)

                // Virtual threshold check
                if (virtualUuids.contains(closestHotspot.uuid) && !closestHotspot.isNotified) {
                    closestHotspot.virtualParticleCount++
                    if (closestHotspot.virtualParticleCount >= 3) {
                        closestHotspot.isNotified = true
                        HotSpotDetectedEventManager.runTasks(closestHotspot)
                    }
                }

                if (closestHotspot.radius > 0) {
                    if (abs(horizontalDistance - closestHotspot.radius) <= 0.05) {
                        cancelable.cancel()
                    }
                } else {
                    cancelable.cancel()
                }
            }
        }
    }

    fun registerHotSpotDetectedEvent(priority: Int = 20, callback: (Hotspot) -> Unit): HotSpotDetectedEventManager.HotSpotDetectedEvent {
        return HotSpotDetectedEventManager.register(priority, callback)
    }

    fun registerHotSpotDisposeEvent(priority: Int = 20, callback: (Hotspot) -> Unit): HotSpotDisposedEventManager.HotSpotDisposedEvent {
        return HotSpotDisposedEventManager.register(priority, callback)
    }

    fun getHotspotAt(pos: Vec3): Hotspot? {
        return hotspots.values.find { hotspot ->
            val horizontalDistance = Vec3(pos.x, 0.0, pos.z).distanceTo(Vec3(hotspot.center.x, 0.0, hotspot.center.z))
            val verticalDistance = abs(pos.y - hotspot.center.y)
            val radius = if (hotspot.radius > 0) hotspot.radius else 6.0
            (horizontalDistance <= radius.toDouble()) && verticalDistance <= 6.0
        }
    }

    object HotSpotDetectedEventManager : AbstractEventManager<(Hotspot) -> Unit, HotSpotDetectedEventManager.HotSpotDetectedEvent>() {
        override val runTasks: (Hotspot) -> Unit = { hotspot ->
            safeExecution {
                tasks.forEach { it.callback(hotspot) }
            }
        }

        fun register(priority: Int = 20, callback: (Hotspot) -> Unit): HotSpotDetectedEvent {
            return HotSpotDetectedEvent(priority, callback).register()
        }

        class HotSpotDetectedEvent(
            priority: Int = 20,
            callback: (Hotspot) -> Unit
        ) : ManagedTask<(Hotspot) -> Unit, HotSpotDetectedEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object HotSpotDisposedEventManager : AbstractEventManager<(Hotspot) -> Unit, HotSpotDisposedEventManager.HotSpotDisposedEvent>() {
        override val runTasks: (Hotspot) -> Unit = { hotspot ->
            safeExecution {
                tasks.forEach { it.callback(hotspot) }
            }
        }

        fun register(priority: Int = 20, callback: (Hotspot) -> Unit): HotSpotDisposedEvent {
            return HotSpotDisposedEvent(priority, callback).register()
        }

        class HotSpotDisposedEvent(
            priority: Int = 20,
            callback: (Hotspot) -> Unit
        ) : ManagedTask<(Hotspot) -> Unit, HotSpotDisposedEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    private fun findBuffNearby(pos: Vec3, world: ClientLevel): String {
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
            buff.contains("Treasure Chance", ignoreCase = true) -> Color(255, 255, 85, 100) // &f
            buff.contains("Fishing Speed", ignoreCase = true) -> Color(85, 255, 255, 100) // &b
            buff.contains("Sea Creature Chance", ignoreCase = true) -> Color(0, 170, 170, 100) // &3
            buff.contains("Double Hook Chance", ignoreCase = true) -> Color(85, 85, 255, 100) // &9
            buff.contains("Trophy Fish Chance", ignoreCase = true) -> Color(255, 170, 0, 100) // &6
            else -> Color(255, 255, 255, 100) // &f
        }
    }

    private fun getLiquidType(pos: Vec3, world: ClientLevel): LiquidTypes {
        for (dx in -1..1) {
            for (dz in -1..1) {
                for (dy in -4..1) {
                    val blockPos = net.minecraft.core.BlockPos.containing(pos.x + dx, pos.y + dy, pos.z + dz)
                    if (world.getBlockState(blockPos).`is`(Blocks.LAVA)) return LiquidTypes.LAVA
                }
            }
        }
        return LiquidTypes.WATER
    }
}
