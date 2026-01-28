package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.access.ConfigScreenInvoker;
import cloud.glitchdev.rfu.access.ListWidgetExtension;
import cloud.glitchdev.rfu.events.managers.CloseConfigEvents;
import com.teamresourceful.resourcefulconfig.client.ConfigScreen;
import com.teamresourceful.resourcefulconfig.client.components.options.OptionsListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ConfigScreen.class)
public abstract class ConfigScreenMixin implements ConfigScreenInvoker {

    @Shadow private OptionsListWidget optionsList;

    @Invoker("clearAndInit")
    protected abstract void invokeClearAndInit();

    @Inject(method = "close", at = @At("HEAD"))
    void onClose(CallbackInfo ci) {
        CloseConfigEvents.INSTANCE.runTasks();
    }

    @Override
    @Unique
    public void rfuReloadAndScroll() {
        double savedScroll = 0.0;
        if (this.optionsList instanceof ListWidgetExtension ext) {
            savedScroll = ext.rfuGetScroll();
        }

        this.invokeClearAndInit();

        if (this.optionsList instanceof ListWidgetExtension ext) {
            ext.rfuSetScroll(savedScroll);
        }
    }
}