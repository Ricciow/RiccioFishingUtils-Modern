package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.CloseConfigEvents;
import com.teamresourceful.resourcefulconfig.client.ConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ConfigScreen.class)
public abstract class ConfigScreenMixin {
    @Invoker("clearAndInit")
    abstract void invokeClearAndInit();

    @Inject(method = "close", at = @At("HEAD"))
    void onClose(CallbackInfo ci) {
        CloseConfigEvents.INSTANCE.runTasks();
    }
}
