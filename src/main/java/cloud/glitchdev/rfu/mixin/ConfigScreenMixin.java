package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.access.ConfigScreenInvoker;
import cloud.glitchdev.rfu.access.ListWidgetExtension;
import cloud.glitchdev.rfu.events.managers.CloseConfigEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfig;
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigElement;
import com.teamresourceful.resourcefulconfig.client.ConfigScreen;
import com.teamresourceful.resourcefulconfig.client.ConfigScreenContext;
import com.teamresourceful.resourcefulconfig.client.components.options.OptionsListWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ConfigScreen.class)
public abstract class ConfigScreenMixin implements ConfigScreenInvoker {

    @Final
    @Shadow private ResourcefulConfig config;
    @Shadow private OptionsListWidget optionsList;
    @Final
    @Shadow private ConfigScreenContext context;

    @Invoker("rebuildWidgets")
    protected abstract void invokeRebuildWidgets();

    @Inject(method = "onClose", at = @At("HEAD"))
    void onClose(CallbackInfo ci) {
        CloseConfigEvents.INSTANCE.getRunTasks().invoke();
    }

    @Override
    @Unique
    public void rfu$ReloadAndScroll() {
        double savedScroll = 0.0;
        if (this.optionsList instanceof ListWidgetExtension ext) {
            savedScroll = ext.rfu$GetScroll();
        }

        this.invokeRebuildWidgets();

        if (this.optionsList instanceof ListWidgetExtension ext) {
            ext.rfu$SetScroll(savedScroll);
        }
    }

    @WrapOperation(
        method = "updateOptions",
        at = @At(
            value = "INVOKE",
            target = "Lcom/teamresourceful/resourcefulconfig/client/components/options/Options;populateOptions(Lcom/teamresourceful/resourcefulconfig/client/components/options/OptionsListWidget;Ljava/util/List;)V"
        )
    )
    private void rfu$wrapPopulateOptions(OptionsListWidget widget, List<ResourcefulConfigElement> elements, Operation<Void> original) {
        if (!rfu$isRFUConfig(this.config)) {
            original.call(widget, elements);
            return;
        }

        List<ResourcefulConfigElement> rfuElements = new ArrayList<>();
        rfu$collectMatchingElements(this.config, rfuElements);
        original.call(widget, rfuElements);
    }

    @Unique
    private boolean rfu$isRFUConfig(ResourcefulConfig config) {
        if (config == null) return false;
        String id = config.id();
        return id != null && id.startsWith("rfu");
    }

    @Unique
    private void rfu$collectMatchingElements(ResourcefulConfig config, List<ResourcefulConfigElement> elements) {
        for (ResourcefulConfigElement element : config.elements()) {
            if (this.context.fulfillsSearch(element)) {
                elements.add(element);
            }
        }
        if (this.context != null && this.context.getQuery() != null && !this.context.getQuery().isBlank()) {
            for (ResourcefulConfig category : config.categories().values()) {
                if (category.info().isHidden()) continue;
                rfu$collectMatchingElements(category, elements);
            }
        }
    }
}