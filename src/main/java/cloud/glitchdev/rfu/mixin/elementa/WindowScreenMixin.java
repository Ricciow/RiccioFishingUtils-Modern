package cloud.glitchdev.rfu.mixin.elementa;

import cloud.glitchdev.rfu.config.categories.OtherSettings;
import gg.essential.elementa.WindowScreen;
import gg.essential.elementa.components.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WindowScreen.class, remap = false)
public abstract class WindowScreenMixin {
    @Shadow @Final private Window window;

    @Inject(method = "onScreenClose", at = @At("TAIL"))
    private void rfu$onScreenClose(CallbackInfo ci) {
        if (OtherSettings.INSTANCE.getPatchElementaMemoryLeaks()) {
            if (this.window != null) {
                this.window.invalidateCachedConstraints();
            }
        }
    }
}
