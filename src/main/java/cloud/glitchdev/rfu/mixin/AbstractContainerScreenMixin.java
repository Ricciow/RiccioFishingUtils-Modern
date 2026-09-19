package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.SlotClickedEvents;
import cloud.glitchdev.rfu.gui.window.HudWindow;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = "slotClicked", at = @At("HEAD"))
    private void onSlotClicked(Slot slot, int slotId, int mouseButton, ContainerInput type, CallbackInfo ci) {
        if (slot != null && slotId >= 0) {
            SlotClickedEvents.INSTANCE.getRunTasks().invoke(slot, ((AbstractContainerScreen<?>) (Object) this));
        }
    }
}