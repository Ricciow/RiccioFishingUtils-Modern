package cloud.glitchdev.rfu.mixin.universalcraft;

import cloud.glitchdev.rfu.gui.window.HudWindow;
import cloud.glitchdev.rfu.gui.components.elementa.TooltipRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import gg.essential.universal.UMatrixStack;
import gg.essential.universal.UScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = UScreen.class, remap = false)
public abstract class UScreenMixin {
    @WrapOperation(
        method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
        at = @At(
            value = "INVOKE",
            target = "Lgg/essential/universal/UScreen;uExtractRenderState(IIF)Lgg/essential/universal/UScreen$RenderState;"
        )
    )
    private UScreen.RenderState rfu$extractWithTooltipContext(
        UScreen instance, int mouseX, int mouseY, float partialTicks,
        Operation<UScreen.RenderState> original,
        @Local(argsOnly = true, name = "context") GuiGraphicsExtractor context
    ) {
        boolean inventoryHud = instance instanceof HudWindow hudWindow
            && hudWindow.getCurrentRenderPass() == HudWindow.RenderPass.INVENTORY;
        if (instance != UScreen.getCurrentScreen() && !inventoryHud) {
            return original.call(instance, mouseX, mouseY, partialTicks);
        }
        return TooltipRenderer.INSTANCE.withContext(
            context, mouseX, mouseY, () -> original.call(instance, mouseX, mouseY, partialTicks)
        );
    }

    @Inject(method = "superDrawScreen(Lgg/essential/universal/UMatrixStack;IIF)V", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("ConstantConditions")
    private void rfu$skipVanillaScreenExtractionForHudPass(UMatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if ((Object) this instanceof HudWindow hudWindow
            && hudWindow.getCurrentRenderPass() != HudWindow.RenderPass.NONE) {
            ci.cancel();
        }
    }
}
