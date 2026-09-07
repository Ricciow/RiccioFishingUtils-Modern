package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.OptionsSaveEvents;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class OptionsMixin {
    @Inject(method = "save", at = @At("HEAD"))
    private void rfu$onBeforeSave(CallbackInfo ci) {
        OptionsSaveEvents.INSTANCE.getRunTasks().invoke(true);
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void rfu$onAfterSave(CallbackInfo ci) {
        OptionsSaveEvents.INSTANCE.getRunTasks().invoke(false);
    }
}
