package cloud.glitchdev.rfu.mixin.screen;

import cloud.glitchdev.rfu.events.managers.PackSelectionScreenEvents;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackSelectionScreen.class)
public class PackSelectionScreenMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PackSelectionScreenEvents.INSTANCE.getRunTasks().invoke((PackSelectionScreen) (Object) this);
    }
}
