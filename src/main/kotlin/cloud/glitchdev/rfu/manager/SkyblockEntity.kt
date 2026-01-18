package cloud.glitchdev.rfu.manager

import cloud.glitchdev.rfu.config.dev.GeneralFishing
import cloud.glitchdev.rfu.events.RenderEvents
import cloud.glitchdev.rfu.events.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.utils.rendering.Render3D.renderSphereOnMob
//? if >=1.21.10 {
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
*///?}
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import java.awt.Color

data class SkyblockEntity(
    val nameTagEntity: ArmorStandEntity,
    val modelEntity: LivingEntity,
    val sbName : String
) {
    var renderEvent : RenderEvents.RenderEvent? = null

    fun isRemoved() : Boolean = nameTagEntity.isRemoved && modelEntity.isRemoved

    fun registerRenderer(renderer : (WorldRenderContext, LivingEntity) -> Unit) {
        if(renderEvent != null) return

        renderEvent = registerRenderEvent { context ->
            renderer(context, this.modelEntity)
        }
    }

    fun registerLsRange() {
        if(GeneralFishing.lootshareRange) {
            registerRenderer { context, entity ->
                renderSphereOnMob(entity, 30f, Color.WHITE, false, context)
            }
        }
    }

    fun dispose() {
        renderEvent?.unregister()
        renderEvent = null
    }
}