package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.EntityFireRenderEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;displayFireAnimation()Z"))
    private boolean rfu$entityFire(Entity entity, Operation<Boolean> original) {
        EntityFireRenderEvents.EntityFireContext context = new EntityFireRenderEvents.EntityFireContext(entity, original.call(entity));
        EntityFireRenderEvents.INSTANCE.getRunTasks().invoke(context);
        return context.getVisible();
    }
}