package cloud.glitchdev.rfu.data.mob

import cloud.glitchdev.rfu.access.EntityAccess
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig.RARE_SC_REGEX
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.events.managers.RenderEvents
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.sphere
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.text
import cloud.glitchdev.rfu.constants.text.TextColor
import gg.essential.universal.utils.toUnformattedString
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import java.awt.Color
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.phys.Vec3
import kotlin.time.Instant
import cloud.glitchdev.rfu.data.fishing.BobberInfo
import cloud.glitchdev.rfu.events.managers.BobberManager
import cloud.glitchdev.rfu.events.managers.ServerCountdownEvents.ServerCountdownEvent
import cloud.glitchdev.rfu.events.managers.ServerCountdownEvents.registerServerCountdownEvent
import cloud.glitchdev.rfu.utils.User

class SkyblockEntity(
    var nameTagEntity: ArmorStand,
    initialModels: Collection<LivingEntity>,
) {
    constructor(nameTagEntity: ArmorStand, modelEntity: LivingEntity) : this(nameTagEntity, listOf(modelEntity))

    val modelEntities: MutableSet<LivingEntity> = initialModels.toMutableSet()
    var modelEntity: LivingEntity = modelEntities.firstOrNull() ?: (nameTagEntity as LivingEntity)

    lateinit var sbName: String
    val createdAt : Instant = Clock.System.now() - (modelEntity.tickCount * 50L).milliseconds
    var health: String = "0"
    var maxHealth: String = "0"
    var isShurikened: Boolean = false
    var originBobber: BobberInfo? = null
    var isDying: Boolean = false

    val parts: MutableSet<Entity> = mutableSetOf()
    private var isGlowing: Boolean = false
    private var glowColor: Color = Color.WHITE

    private val renderers = mutableListOf<(LevelRenderContext, LivingEntity) -> Unit>()
    var renderEvent: RenderEvents.RenderEvent? = null
    private var hasLsRange = false
    private var hasTimer = false
    var countdownEvent: ServerCountdownEvent? = null

    init {
        updateEntityData()
        updateParts()
        linkToBobber()
    }

    fun updateParts() {
        val oldParts = if (isGlowing) parts.toSet() else emptySet()
        parts.clear()
        parts.addAll(modelEntities)

        fun addPassengers(entity: Entity) {
            for (passenger in entity.passengers) {
                if (passenger !== nameTagEntity && parts.add(passenger)) {
                    addPassengers(passenger)
                }
            }
        }

        for (model in modelEntities) {
            var currentVehicle = model.vehicle
            while (currentVehicle != null && currentVehicle !== nameTagEntity) {
                if (parts.add(currentVehicle)) {
                    addPassengers(currentVehicle)
                    currentVehicle = currentVehicle.vehicle
                } else break
            }
            addPassengers(model)
        }

        val world = modelEntity.level()
        if (::sbName.isInitialized) {
            val isSerpentine = MobManager.isSerpentineMob(sbName)
            val maxScan = if (isSerpentine) 32 else 6
            var currentId = nameTagEntity.id - 1
            var scanned = 0
            while (scanned < maxScan) {
                val candidate = world.getEntity(currentId) ?: break
                if (candidate is ArmorStand && (candidate.hasCustomName() || candidate.isCustomNameVisible)) {
                    break
                }
                if (candidate is ArmorStand && candidate.id != nameTagEntity.id &&
                    hasSkull(candidate)) {
                    parts.add(candidate)
                    currentId--
                    scanned++
                } else if (candidate is LivingEntity && (modelEntities.contains(candidate) || candidate.id in (nameTagEntity.id - maxScan until nameTagEntity.id))) {
                    currentId--
                    scanned++
                } else {
                    break
                }
            }
        }

        if (isGlowing) {
            applyGlowToParts(true, glowColor)
            val removedParts = oldParts - parts
            for (removed in removedParts) {
                (removed as? EntityAccess)?.`rfu$setGlowing`(false)
            }
        }
    }

    private fun linkToBobber() {
        if (::sbName.isInitialized) {
            val sc = SeaCreatures.get(sbName)
            if (sc != null) {
                originBobber = parts.firstNotNullOfOrNull { BobberManager.getBobberForEntity(it.id) }
                    ?: BobberManager.findClosestBobber(modelEntity.position(), maxDistance = 1.0)
            }
        }
    }

    fun getName(): String? {
        if (!::sbName.isInitialized) {
            updateEntityData()
        }
        return if (::sbName.isInitialized) sbName else null
    }

    fun isOwn(): Boolean {
        if (originBobber == null) {
            linkToBobber()
        }
        val origin = originBobber ?: return false
        val player = mc.player ?: return false
        val ownerName = origin.ownerName
        return origin.ownerUUID == player.uuid || (ownerName != null && User.isUser(ownerName))
    }

    fun isNamed(name: String, ignoreCase: Boolean = true): Boolean {
        val entityName = getName() ?: return false
        return entityName.contains(name, ignoreCase = ignoreCase)
    }

    fun position(): Vec3 = modelEntity.position()

    override fun toString(): String {
        val bobberInfo = originBobber?.let { " (bobberOwner: ${it.ownerName})" } ?: ""
        return "$sbName ($health/$maxHealth) (models: ${modelEntities.size}, parts: ${parts.size}) (renderers: ${renderers.size})$bobberInfo - ${nameTagEntity.x}, ${nameTagEntity.y}, ${nameTagEntity.z}"
    }

    fun isRemoved(): Boolean {
        if (isDying) return true
        if (modelEntities.isEmpty() || modelEntities.all { !it.isAlive || it.isRemoved }) return true
        val player = mc.player
        return player != null && player.distanceToSqr(position()) <= 400.0 && outdatedNametag()
    }

    fun outdatedNametag(): Boolean = nameTagEntity.isRemoved || (modelEntity.level().getEntity(nameTagEntity.id) == null)

    fun setGlowing(state: Boolean, color: Color = Color.WHITE) {
        this.isGlowing = state
        this.glowColor = color
        applyGlowToParts(state, color)
    }

    private fun hasSkull(candidate: ArmorStand): Boolean {
        return !candidate.getItemBySlot(EquipmentSlot.HEAD).isEmpty
    }

    private fun isEffectivelyInvisible(entity: Entity): Boolean {
        if (!entity.isInvisible) return false
        if (entity is LivingEntity) {
            return entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty
        }
        return true
    }

    private fun applyGlowToParts(state: Boolean, color: Color) {
        parts.forEach { entity ->
            val shouldGlow = state && !isEffectivelyInvisible(entity)
            val access = entity as? EntityAccess ?: return@forEach
            access.`rfu$setGlowing`(shouldGlow)
            access.`rfu$setGlowColor`(color)
        }
    }

    fun isGlowing(): Boolean {
        return isGlowing
    }

    /**
     * Updates the nametag entity if allowed
     * based on the new tag's value.
     */
    fun updateNametag(newTag: ArmorStand) {
        val isSbEntity = isNameTagEntity(newTag)
        if (isSbEntity) {
            this.nameTagEntity = newTag
        } else {
            RFULogger.warn("Attempted to set a non-skyblock nametag to a Skyblock Entity")
        }
    }

    fun updateEntityData() {
        val newData = parseNameTag(nameTagEntity)
        if (newData != null) {
            this.sbName = newData.sbName
            this.health = newData.health
            this.maxHealth = newData.maxHealth
            this.isShurikened = newData.isShurikened
        } else {
            RFULogger.warn("Attempted to update Skyblock Entity Data without a Skyblock Entity")
        }
    }

    fun registerRenderer(renderer: (LevelRenderContext, LivingEntity) -> Unit) {
        renderers.add(renderer)
        if (renderEvent == null) {
            renderEvent = registerRenderEvent { context ->
                for (r in renderers) {
                    r(context, this.modelEntity)
                }
            }
        }
    }

    fun registerLsRange() {
        if (hasLsRange) return
        hasLsRange = true

        registerRenderer { context, entity ->
            val sc = SeaCreatures.get(sbName)
            if (sc != null && SeaCreatureConfig.lootshareRange && RARE_SC_REGEX.matches(sc.scName) && sc.lsRangeEnabled) {
                val bColor = if ((mc.player?.distanceTo(modelEntity) ?: 0f) < 30f) {
                    Color(85, 255, 85)
                } else {
                    Color.WHITE
                }

                Render3D.draw(context) {
                    sphere {
                        pos(entity)
                        radius = 30f
                        borderColor = bColor
                        stacks = 32
                        slices = 32
                        filled = SeaCreatureConfig.filledLsRange
                        if (SeaCreatureConfig.filledLsRange) {
                            color = Color(255, 255, 255, 50)
                        }
                    }
                }
            }
        }
    }

    fun registerTimer(
        totalTicks: Long,
        color: TextColor = TextColor.LIGHT_RED,
        scale: Float = 0.05f,
        condition: () -> Boolean = { true }
    ) {
        countdownEvent?.unregister()
        countdownEvent = registerServerCountdownEvent(totalTicks)

        if (hasTimer) return
        hasTimer = true

        registerRenderer { context, entity ->
            if (!condition()) return@registerRenderer

            val remainingTicks = countdownEvent?.remainingTicks ?: return@registerRenderer
            if (remainingTicks > 0L) {
                val remainingSeconds = remainingTicks / 20.0
                val timerText = "${color.code}${String.format(Locale.US, "%.1fs", remainingSeconds)}"

                Render3D.draw(context) {
                    text {
                        pos(entity, centered = true)
                        text = timerText
                        this.color = Color.WHITE
                        this.scale = scale
                        seeThrough = true
                        dropShadow = true
                        backgroundOpacity = 0.25f
                    }
                }
            }
        }
    }

    fun dispose() {
        if (isGlowing) {
            applyGlowToParts(false, Color.WHITE)
        }
        renderEvent?.unregister()
        renderEvent = null
        renderers.clear()
        countdownEvent?.unregister()
        countdownEvent = null
        hasTimer = false
    }

    data class NameTagData(
        val sbName: String,
        val health: String,
        val maxHealth: String,
        val isShurikened: Boolean,
    )

    companion object {
        private val entityRegex = """(?:﴾ )?\[Lv\d+] \S+ (.+) (\d+[\.,]?\d*[kMB]?)/(\d+[\.,]?\d*[kMB]?)❤(?: ﴿)?( ✯)?""".toRegex(RegexOption.IGNORE_CASE)
        private val corruptedRegex = """^aCorrupted (.+?)a$""".toRegex()

        fun isNameTagEntity(entity: ArmorStand): Boolean {
            if (!entity.hasCustomName()) return false
            val name = entity.name.toUnformattedString()

            return name.matches(entityRegex)
        }

        fun parseNameTag(entity: ArmorStand): NameTagData? {
            if (!entity.hasCustomName()) return null
            val name = entity.name.toUnformattedString()

            val match = entityRegex.find(name) ?: return null

            var sbName = match.groupValues[1].takeIf { it.isNotEmpty() } ?: return null
            sbName = corruptedRegex.find(sbName)?.groupValues?.getOrNull(1) ?: sbName

            val health = match.groupValues[2].ifEmpty { "0" }
            val maxHealth = match.groupValues[3].ifEmpty { "0" }
            val isShurikened = match.groups[4] != null

            return NameTagData(sbName, health, maxHealth, isShurikened)
        }
    }
}
