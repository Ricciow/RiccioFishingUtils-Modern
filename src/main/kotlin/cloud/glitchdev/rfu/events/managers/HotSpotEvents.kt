package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.HotSpotConstants
import cloud.glitchdev.rfu.constants.HotspotType
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.data.fishing.HotspotCache
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ParticleEvents.registerParticleEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.events.managers.EntityRenderEvents.registerEntityRenderEvent
import cloud.glitchdev.rfu.utils.World
import gg.essential.universal.utils.toUnformattedString
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.StringSetEntry
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.awt.Color
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@AutoRegister
object HotSpotEvents : RegisteredEvent {
    private val hotspots = ConcurrentHashMap<UUID, Hotspot>()
    private val virtualUuids = mutableSetOf<UUID>()

    override fun register() {
        HotspotCache.getCachedEntries(null)

        //Rfu
        registerGameEvent("""Party > (?:\[[A-Z]+\+*] )?([0-9a-zA-Z_]{3,16}): (.*?) Hotspot - (-?\d+), (-?\d+), (-?\d+)""".toExactRegex()) { _, _, matches ->
            val groups = matches?.groupValues ?: return@registerGameEvent
            handleHotspotMessage(
                sender = groups[1],
                stat = groups[2],
                x = groups[3].toDouble(),
                y = groups[4].toDouble(),
                z = groups[5].toDouble()
            )
        }

        //Feesh
        registerGameEvent("""(?:\[[A-Z]+\+*] )?([0-9a-zA-Z_]{3,16}): x: (-?\d+), y: (-?\d+), z: (-?\d+) \| .{3} (.*?) Hotspot at .+""".toRegex()) { _, _, matches ->
            val groups = matches?.groupValues ?: return@registerGameEvent
            handleHotspotMessage(
                sender = groups[1],
                stat = groups[5],
                x = groups[2].toDouble(),
                y = groups[3].toDouble(),
                z = groups[4].toDouble()
            )
        }

        registerTickEvent(interval = 2) { client ->
            val world = client.level ?: return@registerTickEvent
            val entities = world.entitiesForRendering()
            val seenUuids = mutableSetOf<UUID>()
            val now = System.currentTimeMillis()

            entities.forEach { entity ->
                val isHotspot = entity.customName?.toUnformattedString()?.contains("HOTSPOT") ?: return@forEach

                if (isHotspot) {
                    val uuid = entity.uuid
                    seenUuids.add(uuid)

                    if (!hotspots.containsKey(uuid)) {
                        val pos = entity.position()
                        val blockPos = BlockPos.containing(pos.x, pos.y, pos.z)

                        // Check if there is a virtual hotspot at this location and remove it
                        val virtualUuid = UUID.nameUUIDFromBytes("virtual_${blockPos}".toByteArray())
                        if (hotspots.containsKey(virtualUuid)) {
                            val virtualHotspot = hotspots.remove(virtualUuid)
                            virtualUuids.remove(virtualUuid)
                            if (virtualHotspot != null && virtualHotspot.isNotified) {
                                HotSpotDisposedEventManager.runTasks(virtualHotspot)
                            }
                        }

                        // Try to find cached buff first, otherwise start as UNKNOWN
                        val cachedBuff = findCachedBuff(blockPos, World.island)
                        val buff = cachedBuff ?: ""
                        val liquid = getLiquidType(pos, world)
                        val hotspot = Hotspot(uuid, pos, buff, 0f, liquid)
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
                    val player = client.player
                    val distanceToPlayer = player?.position()?.distanceTo(hotspot.center) ?: 0.0

                    if (distanceToPlayer < HotSpotConstants.RANGE_DISPOSE_DISTANCE) {
                        if (hotspot.rangeEntryTime == null) {
                            hotspot.rangeEntryTime = now
                        }

                        if (now - (hotspot.rangeEntryTime ?: now) > HotSpotConstants.RANGE_ENTRY_TIMEOUT_MS && now - hotspot.lastUpdate > HotSpotConstants.RANGE_ENTRY_TIMEOUT_MS) {
                            iterator.remove()
                            virtualUuids.remove(uuid)
                            if (hotspot.isNotified) {
                                HotSpotDisposedEventManager.runTasks(hotspot)
                            }
                            continue
                        }
                    } else {
                        hotspot.rangeEntryTime = null
                    }

                    virtualUuids.add(uuid)

                    val timeout = if (hotspot.isExternal) HotSpotConstants.EXTERNAL_TIMEOUT_MS else HotSpotConstants.INACTIVITY_TIMEOUT_MS
                    if (now - hotspot.lastUpdate > timeout) {
                        iterator.remove()
                        virtualUuids.remove(uuid)
                        if (hotspot.isNotified) {
                            HotSpotDisposedEventManager.runTasks(hotspot)
                        }
                    }
                }
            }
        }

        registerEntityRenderEvent { entity, isVisible, _ ->
            if (!isVisible) return@registerEntityRenderEvent
            val name = entity.customName?.toUnformattedString() ?: return@registerEntityRenderEvent
            if (HotspotType.entries.any { it.buffMatch != null && name.contains(it.buffMatch) }) {
                val pos = entity.position()
                val unknownHotspot = hotspots.values
                    .filter { it.type == HotspotType.UNKNOWN }
                    .minByOrNull { it.center.distanceTo(pos) }

                if (unknownHotspot != null && unknownHotspot.center.distanceTo(pos) < 5.0) {
                    unknownHotspot.buff = name
                    HotspotCache.addMeasurement(unknownHotspot.blockPos, 0.0, unknownHotspot.liquid, name, unknownHotspot.island)
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

                if(red != HotSpotConstants.PARTICLE_RED || green != HotSpotConstants.PARTICLE_GREEN || blue != HotSpotConstants.PARTICLE_BLUE) return@registerParticleEvent
            }

            val pos = Vec3(packet.x, packet.y, packet.z)

            var closestHotspot = hotspots.values
                .filter { hotspot ->
                    if (isSmoke) hotspot.liquid == LiquidTypes.LAVA
                    else hotspot.liquid == LiquidTypes.WATER
                }
                .minByOrNull { it.center.distanceTo(pos) }

            if (closestHotspot == null || abs(pos.y - closestHotspot.center.y) > HotSpotConstants.PARTICLE_MAX_VERTICAL_DISTANCE || pos.horizontalDistance(closestHotspot.center) > HotSpotConstants.PARTICLE_MAX_HORIZONTAL_DISTANCE) {
                val playerPos = RiccioFishingUtils.mc.player?.position() ?: return@registerParticleEvent
                val cachedEntry = HotspotCache.getCachedEntries(World.island).find { (blockPos, data) ->
                    val center = Vec3(blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5)

                    if (playerPos.distanceTo(center) < HotSpotConstants.RANGE_DISPOSE_DISTANCE) return@find false

                    val liquidMatches = if (isSmoke) data.liquid == LiquidTypes.LAVA else data.liquid == LiquidTypes.WATER
                    liquidMatches && abs(pos.y - center.y) <= HotSpotConstants.PARTICLE_MAX_VERTICAL_DISTANCE && pos.horizontalDistance(center) <= HotSpotConstants.PARTICLE_MAX_HORIZONTAL_DISTANCE
                }

                if (cachedEntry != null) {
                    val (blockPos, data) = cachedEntry
                    val center = Vec3(blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5)
                    val uuid = UUID.nameUUIDFromBytes("virtual_${blockPos}".toByteArray())

                    val now = System.currentTimeMillis()
                    val buff = if (now - data.lastMetadataUpdate < HotSpotConstants.METADATA_EXPIRY_MS) data.sessionBuff else ""

                    closestHotspot = hotspots.getOrPut(uuid) {
                        virtualUuids.add(uuid)
                        Hotspot(uuid, center, buff, 0f, data.liquid)
                    }
                }
            }

            if (closestHotspot == null) return@registerParticleEvent
            if (abs(pos.y - closestHotspot.center.y) > HotSpotConstants.PARTICLE_MAX_VERTICAL_DISTANCE) return@registerParticleEvent

            val horizontalDistance = pos.horizontalDistance(closestHotspot.center)

            if (horizontalDistance < HotSpotConstants.PARTICLE_MAX_HORIZONTAL_DISTANCE) {
                if (virtualUuids.contains(closestHotspot.uuid)) {
                    val inRing = closestHotspot.radius > 0 && abs(horizontalDistance - closestHotspot.radius) <= HotSpotConstants.RADIUS_CANCELLATION_TOLERANCE
                    if (inRing) {
                        closestHotspot.lastUpdate = System.currentTimeMillis()
                    }
                } else {
                    closestHotspot.addParticleDistance(horizontalDistance)
                }

                // Virtual threshold check
                if (virtualUuids.contains(closestHotspot.uuid) && !closestHotspot.isNotified) {
                    closestHotspot.virtualParticleCount++
                    if (closestHotspot.virtualParticleCount >= HotSpotConstants.VIRTUAL_PARTICLE_THRESHOLD) {
                        closestHotspot.isNotified = true
                        HotSpotDetectedEventManager.runTasks(closestHotspot)
                    }
                }

                if (closestHotspot.radius > 0 && HotSpotSettings.highlightHotSpots) {
                    if (abs(horizontalDistance - closestHotspot.radius) <= HotSpotConstants.RADIUS_CANCELLATION_TOLERANCE) {
                        cancelable.cancel()
                    }
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
            val horizontalDistance = pos.horizontalDistance(hotspot.center)
            val verticalDistance = abs(pos.y - hotspot.center.y)
            val radius = if (hotspot.radius > 0) hotspot.radius.toDouble() else HotSpotConstants.PARTICLE_DETECTION_RADIUS
            (horizontalDistance <= radius) && verticalDistance <= HotSpotConstants.PARTICLE_MAX_VERTICAL_DISTANCE
        }
    }

    fun getAllHotspots(): Collection<Hotspot> = hotspots.values

    fun addExternalHotspot(pos: Vec3, type: HotspotType) {
        val existing = getHotspotAt(pos)
        if (existing != null) {
            if (existing.type == HotspotType.UNKNOWN && type != HotspotType.UNKNOWN) {
                existing.buff = type.displayName
            }
            return
        }

        val blockPos = BlockPos.containing(pos.x, pos.y, pos.z)
        val uuid = UUID.nameUUIDFromBytes("virtual_${blockPos}".toByteArray())
        if (hotspots.containsKey(uuid)) return

        val hotspot = Hotspot(uuid, pos, type.displayName, 0f, LiquidTypes.WATER).apply {
            isNotified = true
            isExternal = true
        }

        hotspots[uuid] = hotspot
        virtualUuids.add(uuid)
        HotSpotDetectedEventManager.runTasks(hotspot)
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
        val searchBox = AABB(
            pos.x - HotSpotConstants.BUFF_SEARCH_HORIZONTAL, pos.y - HotSpotConstants.BUFF_SEARCH_VERTICAL, pos.z - HotSpotConstants.BUFF_SEARCH_HORIZONTAL, 
            pos.x + HotSpotConstants.BUFF_SEARCH_HORIZONTAL, pos.y + HotSpotConstants.BUFF_SEARCH_VERTICAL, pos.z + HotSpotConstants.BUFF_SEARCH_HORIZONTAL
        )
        
        val entities = world.getEntitiesOfClass(ArmorStand::class.java, searchBox).toList()
        
        for (entity in entities) {
            val name = entity.customName?.toUnformattedString() ?: continue
            if (HotspotType.entries.any { it.buffMatch != null && name.contains(it.buffMatch) }) {
                return name
            }
        }
        return ""
    }

    private fun getLiquidType(pos: Vec3, world: ClientLevel): LiquidTypes {
        val horizontal = HotSpotConstants.LIQUID_SEARCH_HORIZONTAL
        for (dx in -horizontal..horizontal) {
            for (dz in -horizontal..horizontal) {
                for (dy in HotSpotConstants.LIQUID_SEARCH_VERTICAL_MIN..HotSpotConstants.LIQUID_SEARCH_VERTICAL_MAX) {
                    val blockPos = BlockPos.containing(pos.x + dx, pos.y + dy, pos.z + dz)
                    if (world.getBlockState(blockPos).`is`(Blocks.LAVA)) return LiquidTypes.LAVA
                }
            }
        }
        return LiquidTypes.WATER
    }

    private fun Vec3.horizontalDistance(pos: Vec3): Double {
        return sqrt((this.x - pos.x).pow(2.0) + (this.z - pos.z).pow(2.0))
    }

    private fun findCachedBuff(pos: BlockPos, island: FishingIslands?): String? {
        val cached = HotspotCache.getCachedEntries(island).find { it.first == pos }
        val data = cached?.second ?: return null
        val now = System.currentTimeMillis()
        return if (now - data.lastMetadataUpdate < HotSpotConstants.METADATA_EXPIRY_MS) data.sessionBuff else null
    }

    fun clearHotspots() {
        hotspots.clear()
        virtualUuids.clear()
    }

    private fun handleHotspotMessage(sender: String, stat: String, x: Double, y: Double, z: Double) {
        if (sender.equals(RiccioFishingUtils.mc.player?.name?.string, ignoreCase = true)) return

        val ignoredEntry = OtherManager.getField("ignored_users") { StringSetEntry() } as StringSetEntry
        if (ignoredEntry.contains(sender)) return

        val pos = Vec3(x, y, z)
        val type = HotspotType.fromBuff(stat)

        Chat.sendMessage(
            TextUtils.rfuLiteral("${TextColor.YELLOW}Received a ")
                .append(
                    Component.literal(type.displayName)
                        .withStyle(Style.EMPTY.withColor(type.color.rgb))
                ).append(
                    " ${TextColor.YELLOW}hotspot's coordinates from ${TextColor.GOLD}$sender"
                ).setStyle(
                    Style.EMPTY
                        .withHoverEvent(HoverEvent.ShowText(Component.literal("${TextColor.YELLOW}Click to ignore this user in the future!\n${TextColor.GRAY}(Will also block them from party finder and party commands)")))
                        .withClickEvent(ClickEvent.RunCommand("/rfuignore add $sender"))
                )
        )
        addExternalHotspot(pos, type)
    }
}
