package cloud.glitchdev.rfu.data.mob

import cloud.glitchdev.rfu.access.EntityAccess
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig.RARE_SC_REGEX
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.EventScopes
import cloud.glitchdev.rfu.events.managers.render.registerRenderEvent
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
import cloud.glitchdev.rfu.events.managers.fishing.BobberManager
import cloud.glitchdev.rfu.events.managers.lifecycle.ServerCountdownEvents.ServerCountdownEvent
import cloud.glitchdev.rfu.events.managers.lifecycle.registerServerCountdownEvent
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.dsl.parseHealthValue

class SkyblockEntity(
    var nameTagEntity: ArmorStand,
    initialModels: Collection<LivingEntity>,
    initialCreatedAt: Instant? = null,
    initialOriginBobber: BobberInfo? = null,
    val wasRedetected: Boolean = false,
) {
    constructor(nameTagEntity: ArmorStand, modelEntity: LivingEntity) : this(nameTagEntity, listOf(modelEntity))

    val modelEntities: MutableSet<LivingEntity> = initialModels.toMutableSet()
    val modelIds: MutableSet<Int> = initialModels.mapTo(mutableSetOf()) { it.id }
    var modelEntity: LivingEntity = modelEntities.firstOrNull() ?: (nameTagEntity as LivingEntity)

    lateinit var sbName: String
    val createdAt : Instant = initialCreatedAt ?: (Clock.System.now() - (modelEntity.tickCount * 50L).milliseconds)
    private var cachedHealth: Long? = null
    private var cachedMaxHealth: Long? = null

    var healthStr: String = "0"
        set(value) {
            if (field == value) return
            field = value
            cachedHealth = null
        }
    var maxHealthStr: String = "0"
        set(value) {
            if (field == value) return
            field = value
            cachedMaxHealth = null
        }

    val health: Long
        get() = cachedHealth ?: healthStr.parseHealthValue().also { cachedHealth = it }
    val maxHealth: Long
        get() = cachedMaxHealth ?: maxHealthStr.parseHealthValue().also { cachedMaxHealth = it }
    var hasHealth: Boolean = false
        private set
    var isShurikened: Boolean = false
    var originBobber: BobberInfo? = initialOriginBobber
    var isDying: Boolean = false

    val parts: MutableSet<Entity> = mutableSetOf()
    private var isGlowing: Boolean = false
    private var glowColor: Color = Color.WHITE

    private val eventScopes = EventScopes()
    private var countdownScope: EventScope? = null
    private var disposed = false
    private val renderers = mutableListOf<(LevelRenderContext, LivingEntity) -> Unit>()
    private var hasLsRange = false
    private var hasTimer = false
    private var timerCondition: () -> Boolean = { true }
    var countdownEvent: ServerCountdownEvent? = null
        private set

    init {
        updateEntityData()
        updateParts()
        if (!wasRedetected && originBobber == null) {
            linkToBobber()
        }
    }

    fun addModelEntities(models: Collection<LivingEntity>) {
        modelEntities.addAll(models)
        models.forEach { modelIds.add(it.id) }
        updateParts()
        if (!wasRedetected && originBobber == null && ::sbName.isInitialized && SeaCreatures.get(sbName) != null) {
            originBobber = parts.firstNotNullOfOrNull { BobberManager.getBobberForEntity(it.id) }
        }
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
                    ?: if (modelEntity.tickCount <= 20) {
                        BobberManager.findClosestBobber(modelEntity.position(), maxDistance = 1.0)
                    } else {
                        null
                    }
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
        return "$sbName ($healthStr/$maxHealthStr) (models: ${modelEntities.size}, parts: ${parts.size}) (renderers: ${renderers.size})$bobberInfo - ${nameTagEntity.x}, ${nameTagEntity.y}, ${nameTagEntity.z}"
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
            this.healthStr = newData.healthStr
            this.maxHealthStr = newData.maxHealthStr
            this.hasHealth = newData.hasHealth
            this.isShurikened = newData.isShurikened
        } else {
            RFULogger.warn("Attempted to update Skyblock Entity Data without a Skyblock Entity")
        }
    }

    fun registerRenderer(renderer: (LevelRenderContext, LivingEntity) -> Unit) {
        if (disposed) return
        eventScopes.load(::initializeListeners)
        renderers.add(renderer)
    }

    private fun initializeListeners(scope: EventScope) {
        scope.registerRenderEvent { context ->
            for (renderer in renderers) {
                renderer(context, modelEntity)
            }
        }
        countdownEvent?.takeUnless { it.isCompleted }?.let { timer ->
            val countdown = scope.child()
            countdownScope = countdown
            countdown.registerTask(timer::register)
        }
        scope.onClose { countdownScope = null }
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
        label: String? = null,
        condition: () -> Boolean = { true }
    ) {
        if (disposed) return
        countdownScope?.close()
        countdownScope = null
        countdownEvent = null
        timerCondition = condition
        val scope = eventScopes.load(::initializeListeners)?.child()
        if (scope != null) {
            countdownScope = scope
            countdownEvent = scope.registerServerCountdownEvent(totalTicks)
        }

        if (hasTimer) return
        hasTimer = true

        registerTextDisplay(color, scale) {
            if (!timerCondition()) return@registerTextDisplay null
            val remainingTicks = countdownEvent?.remainingTicks?.takeIf { it > 0L }
                ?: return@registerTextDisplay null
            val timerText = String.format(Locale.US, "%.1fs", remainingTicks / 20.0)
            if (label == null) timerText else "$label\n$timerText"
        }
    }

    fun registerTextDisplay(
        color: TextColor = TextColor.LIGHT_RED,
        scale: Float = 0.05f,
        content: () -> String?
    ) {
        registerRenderer { context, entity ->
            val displayText = content() ?: return@registerRenderer
            Render3D.draw(context) {
                text {
                    pos(entity, centered = true)
                    text = displayText.lines().joinToString("\n") { "${color.code}$it" }
                    this.color = Color.WHITE
                    this.scale = scale
                    seeThrough = true
                    dropShadow = true
                    backgroundOpacity = 0.25f
                }
            }
        }
    }

    fun dispose() {
        if (disposed) return
        disposed = true
        try {
            if (isGlowing) {
                applyGlowToParts(false, Color.WHITE)
            }
        } finally {
            countdownScope = null
            countdownEvent = null
            renderers.clear()
            hasTimer = false
            timerCondition = { true }
            eventScopes.close()
        }
    }

    data class NameTagData(
        val sbName: String,
        val healthStr: String,
        val maxHealthStr: String,
        val isShurikened: Boolean,
        val hasHealth: Boolean,
    )

    companion object {
        private val entityRegex = """(?:﴾ )?\[Lv\d+] \S+ (.+?)(?: (\d+[\.,]?\d*[kMB]?)/(\d+[\.,]?\d*[kMB]?)❤)?(?: ﴿)?( [✯\uE01A])?""".toRegex(RegexOption.IGNORE_CASE)
        private val corruptedRegex = """^aCorrupted (.+?)a$""".toRegex()

        fun isNameTagEntity(entity: ArmorStand): Boolean {
            return parseNameTag(entity) != null
        }

        fun parseNameTag(entity: ArmorStand): NameTagData? {
            if (!entity.hasCustomName()) return null
            val name = entity.name.toUnformattedString()

            val match = entityRegex.matchEntire(name) ?: return null

            var sbName = match.groupValues[1].takeIf { it.isNotEmpty() } ?: return null
            sbName = corruptedRegex.find(sbName)?.groupValues?.getOrNull(1) ?: sbName

            val healthStr = match.groupValues[2].ifEmpty { "0" }
            val maxHealthStr = match.groupValues[3].ifEmpty { "0" }
            val isShurikened = match.groups[4] != null

            return NameTagData(sbName, healthStr, maxHealthStr, isShurikened, hasHealth = match.groups[2] != null)
        }
    }
}
