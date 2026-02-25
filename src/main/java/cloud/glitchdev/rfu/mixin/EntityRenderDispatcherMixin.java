package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.EntityRenderEvents;
import cloud.glitchdev.rfu.events.wrappers.CancelableBoolean;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(
            method = "shouldRender",
            at = @At("HEAD"),
            cancellable = true
    )
    private <E extends Entity> void onShouldRender(
            E entity,
            Frustum frustum,
            double x,
            double y,
            double z,
            CallbackInfoReturnable<Boolean> cir
    ) {
        CancelableBoolean cancelWrapper = new CancelableBoolean(cir, false);
        EntityRenderEvents.INSTANCE.runTasks(entity, cancelWrapper);
    }
}