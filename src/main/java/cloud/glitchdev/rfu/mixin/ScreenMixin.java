package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.gui.window.HudWindow;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @WrapOperation(
        method = "extractRenderStateWithTooltipAndSubtitles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;extractDeferredElements(IIF)V"
        )
    )
    private void rfu$renderHudOnInventory(GuiGraphicsExtractor instance, int mouseX, int mouseY, float a, Operation<Void> original) {
        if ((Screen) (Object) this instanceof AbstractContainerScreen) {
            instance.nextStratum();
            HudWindow.INSTANCE.renderOnInventory(instance, mouseX, mouseY, a);
            instance.nextStratum();
        }
        original.call(instance, mouseX, mouseY, a);
    }
}
