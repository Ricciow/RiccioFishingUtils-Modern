package cloud.glitchdev.rfu.mixin.universalcraft;

import cloud.glitchdev.rfu.gui.window.HudWindow;
import gg.essential.universal.UMatrixStack;
import gg.essential.universal.UScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = UScreen.class, remap = false)
public abstract class UScreenMixin {
    @Inject(method = "superDrawScreen(Lgg/essential/universal/UMatrixStack;IIF)V", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("ConstantConditions")
    private void rfu$skipVanillaScreenExtractionForHudPass(UMatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if ((Object) this instanceof HudWindow hudWindow
            && hudWindow.getCurrentRenderPass() != HudWindow.RenderPass.NONE) {
            ci.cancel();
        }
    }
}
