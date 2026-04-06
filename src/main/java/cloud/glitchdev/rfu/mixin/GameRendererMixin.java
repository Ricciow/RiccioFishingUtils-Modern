package cloud.glitchdev.rfu.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
//?if < 26.1 {
/*import cloud.glitchdev.rfu.events.managers.FovEvents;
import cloud.glitchdev.rfu.events.wrappers.Cancelable;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    //? if < 26.1 {
    /*@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float partialTick, boolean useFOVSetting, CallbackInfoReturnable<Float> cir) {
        Cancelable<Float> cancelable = new Cancelable<>(cir, null);
        FovEvents.INSTANCE.getRunTasks().invoke(cancelable, partialTick);
    }
    *///?}
}