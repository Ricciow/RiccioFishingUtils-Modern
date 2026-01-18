package cloud.glitchdev.rfu.manager

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
    var isRegistered = false

    fun isRemoved() : Boolean = nameTagEntity.isRemoved && modelEntity.isRemoved

    fun registerRenderer(renderer : (WorldRenderContext, LivingEntity) -> Unit) {
        if(isRegistered) return

        registerRenderEvent { context ->
            renderer(context, this.modelEntity)

            !isRemoved() && isRegistered
        }

        isRegistered = true
    }

    fun registerLsRange() {
        registerRenderer { context, entity ->
            renderSphereOnMob(entity, 30f, Color.WHITE, false, context)
        }
    }
}