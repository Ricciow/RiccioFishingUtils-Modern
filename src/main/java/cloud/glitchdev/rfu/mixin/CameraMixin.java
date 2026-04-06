package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.FovEvents;
import cloud.glitchdev.rfu.events.wrappers.Cancelable;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {
    //? if >= 26.1 {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void onCalculateFov(float partialTicks, CallbackInfoReturnable<Float> cir) {
        Cancelable<Float> cancelable = new Cancelable<>(cir, null);
        FovEvents.INSTANCE.getRunTasks().invoke(cancelable, partialTicks);
    }
    //?}
}