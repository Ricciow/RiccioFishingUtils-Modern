package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ParticleEvents.registerParticleEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.multiplayer.ClientLevel
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

@AutoRegister
object HotSpotEvents : RegisteredEvent {
    private val hotspots = ConcurrentHashMap<UUID, Hotspot>()

    override fun register() {
        registerTickEvent(interval = 20) { client ->
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
                        val liquid = getLiquidType(pos, world)
                        val hotspot = Hotspot(uuid, pos, buff, 0f, color, liquid)
                        hotspots[uuid] = hotspot
                        HotSpotDetectedEventManager.runTasks(hotspot)
                    }
                }
            }

            val iterator = hotspots.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (!seenUuids.contains(entry.key)) {
                    val hotspot = entry.value
                    iterator.remove()
                    HotSpotDisposedEventManager.runTasks(hotspot)
                }
            }
        }

        registerParticleEvent { packet, cancelable ->
            if (!isAllowedParticle(packet.particle)) return@registerParticleEvent

            val pos = Vec3(packet.x, packet.y, packet.z)
            val closestHotspot = hotspots.values.minByOrNull { it.center.distanceTo(pos) } ?: return@registerParticleEvent

            if (abs(pos.y - closestHotspot.center.y) > 6.0) return@registerParticleEvent

            val horizontalDistance = Vec3(pos.x, 0.0, pos.z).distanceTo(Vec3(closestHotspot.center.x, 0.0, closestHotspot.center.z))

            if(closestHotspot.isRadiusCalculated()) {
                if(abs(horizontalDistance - closestHotspot.radius) <= 0.05) {
                    cancelable.cancel()
                }
                return@registerParticleEvent
            }

            if (horizontalDistance < 6.0) {
                closestHotspot.addParticleDistance(horizontalDistance)
                cancelable.cancel()
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

    private fun isAllowedParticle(particle : ParticleOptions) : Boolean {
        val result = when(particle.type) {
            ParticleTypes.DUST -> true
            ParticleTypes.SMOKE -> true
            else -> false
        }

        return result
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
