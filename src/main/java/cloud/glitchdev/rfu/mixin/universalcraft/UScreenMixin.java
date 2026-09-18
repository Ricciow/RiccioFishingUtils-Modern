package cloud.glitchdev.rfu.mixin.universalcraft;

import gg.essential.universal.UScreen;
import org.spongepowered.asm.mixin.Mixin;
//? if <26.3 {
/*import cloud.glitchdev.rfu.gui.window.HudWindow;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.essential.universal.AdvancedDrawContext;
import org.spongepowered.asm.mixin.injection.At;
*///?}

@Mixin(value = UScreen.class, remap = false)
public abstract class UScreenMixin {
    //? if <26.3 {
    /*@WrapOperation(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lgg/essential/universal/AdvancedDrawContext;nextFrame()V"
        )
    )
    private void rfu$wrapNextFrame(AdvancedDrawContext instance, Operation<Void> original) {
        if ((Object) this == HudWindow.INSTANCE && HudWindow.INSTANCE.getCurrentRenderPass() == HudWindow.RenderPass.INVENTORY) {
            return;
        }
        original.call(instance);
    }
    *///?}
}
