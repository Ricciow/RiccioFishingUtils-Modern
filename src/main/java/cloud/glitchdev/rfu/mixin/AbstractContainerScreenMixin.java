package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.SlotClickedEvents;
import cloud.glitchdev.rfu.gui.window.HudWindow;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void rfu$onInit(CallbackInfo ci) {
        HudWindow.INSTANCE.onInventoryOpened((AbstractContainerScreen<?>) (Object) this);
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void rfu$onRemoved(CallbackInfo ci) {
        HudWindow.INSTANCE.onInventoryClosed();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void rfu$onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (HudWindow.INSTANCE.handleMouseClicked(event.x(), event.y(), event.button())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void rfu$onMouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (HudWindow.INSTANCE.handleMouseReleased(event.x(), event.y(), event.button())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void rfu$onMouseDragged(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        if (HudWindow.INSTANCE.handleMouseDragged(event.x(), event.y(), event.button(), dx, dy)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void rfu$onMouseScrolled(double x, double y, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (HudWindow.INSTANCE.handleMouseScrolled(x, y, scrollX, scrollY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "slotClicked", at = @At("HEAD"))
    private void onSlotClicked(Slot slot, int slotId, int mouseButton, ContainerInput type, CallbackInfo ci) {
        if (slot != null && slotId >= 0) {
            SlotClickedEvents.INSTANCE.getRunTasks().invoke(slot, ((AbstractContainerScreen<?>) (Object) this));
        }
    }
}
