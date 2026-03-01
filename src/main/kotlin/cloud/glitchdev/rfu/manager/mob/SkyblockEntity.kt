package cloud.glitchdev.rfu.manager.mob

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.GeneralFishing.RARE_SC_REGEX
import cloud.glitchdev.rfu.events.managers.RenderEvents
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.rendering.Render3D.renderSphereOnMob
import gg.essential.universal.utils.toUnformattedString
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import java.awt.Color

class SkyblockEntity(
    var nameTagEntity: ArmorStand,
    var modelEntity: LivingEntity,
) {
    lateinit var sbName: String
    var health: String = "0"
    var maxHealth: String = "0"
    var isShurikened: Boolean = false

    var renderEvent: RenderEvents.RenderEvent? = null

    override fun toString(): String {
        return "$sbName ($health/$maxHealth) (renderEvent: ${renderEvent != null}) - ${nameTagEntity.x}, ${nameTagEntity.y}, ${nameTagEntity.z}"
    }

    fun isRemoved(): Boolean = nameTagEntity.isRemoved && modelEntity.isRemoved

    fun outdatedNametag() : Boolean = nameTagEntity.isRemoved

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

    fun registerRenderer(renderer: (WorldRenderContext, LivingEntity) -> Unit) {
        if (renderEvent != null) return

        renderEvent = registerRenderEvent { context ->
            renderer(context, this.modelEntity)
        }
    }

    fun registerLsRange() {
        registerRenderer { context, entity ->
            if (GeneralFishing.lootshareRange && RARE_SC_REGEX.matches(sbName)) {
                renderSphereOnMob(entity, 30f, Color.WHITE, false, context)
            }
        }
    }

    fun dispose() {
        renderEvent?.unregister()
        renderEvent = null
    }

    data class NameTagData(
        val sbName: String,
        val health: String,
        val maxHealth: String,
        val isShurikened: Boolean,
    )

    companion object {
        private val entityRegex = """(?:﴾ )?\[Lv\d+] \S+ (.+) (\d+[\.,]?\d*[kM]?)/(\d+[\.,]?\d*[kM]?)❤(?: ﴿)?( ✯)?""".toRegex()
        private val corruptedRegex = """^aCorrupted (.+)a$""".toRegex()

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