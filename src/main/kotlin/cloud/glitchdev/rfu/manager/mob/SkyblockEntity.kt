package cloud.glitchdev.rfu.manager.mob

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.RenderEvents
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.feature.mob.LootshareRange.RARE_SC_REGEX
import cloud.glitchdev.rfu.utils.rendering.Render3D.renderSphereOnMob
import gg.essential.universal.utils.toUnformattedString
//? if >=1.21.10 {
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
*///?}
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import java.awt.Color

data class SkyblockEntity(
    var nameTagEntity: ArmorStandEntity,
    val modelEntity: LivingEntity,
    var sbName: String
) {
    var renderEvent: RenderEvents.RenderEvent? = null

    companion object {
        private val entityRegex = """(?:﴾ )?\[Lv\d+\] [^\s]+ (.+) \d+[\.,]?\d*[kM]?\/\d+[\.,]?\d*[kM]?❤(?: ✯)?(?: ﴿)?""".toRegex()
        private val corruptedRegex = """^aCorrupted (.+)a$""".toRegex()

        fun parseNameFromTag(entity: ArmorStandEntity): String? {
            if (!entity.hasCustomName()) return null
            val name = entity.name.toUnformattedString()

            if (!name.matches(entityRegex)) return null

            var sbName = entityRegex.find(name)?.groupValues?.getOrNull(1) ?: return null
            sbName = corruptedRegex.find(sbName)?.groupValues?.getOrNull(1) ?: sbName

            return sbName
        }
    }

    fun isRemoved(): Boolean = nameTagEntity.isRemoved && modelEntity.isRemoved

    /**
     * Updates the nametag entity and automatically refreshes the sbName
     * based on the new tag's value.
     */
    fun updateNametag(newTag: ArmorStandEntity) {
        val newName = parseNameFromTag(newTag)
        if (newName != null) {
            this.nameTagEntity = newTag
            this.sbName = newName
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
}