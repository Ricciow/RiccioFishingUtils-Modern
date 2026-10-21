package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.access.ConfigScreenInvoker;
import cloud.glitchdev.rfu.access.ListWidgetExtension;
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigObjectEntry;
import com.teamresourceful.resourcefulconfig.client.components.options.Options;
import com.teamresourceful.resourcefulconfig.client.components.options.OptionsListWidget;
import com.teamresourceful.resourcefulconfig.client.screens.base.ModalOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "com.teamresourceful.resourcefulconfig.client.components.options.types.ObjectOptionWidget$ObjectEditOverlay")
public abstract class ConfigObjectScreenMixin extends ModalOverlay implements ConfigScreenInvoker {
    @Shadow(remap = false) @Final private ResourcefulConfigObjectEntry entry;

    @Override
    @Unique
    public void rfu$ReloadAndScroll() {
        for (var child : children()) {
            if (!(child instanceof OptionsListWidget list)) continue;

            double scroll = list instanceof ListWidgetExtension ext ? ext.rfu$GetScroll() : 0.0;
            list.setFocused(null);
            list.clear();
            Options.populateOptions(list, this.entry.elements());
            if (list instanceof ListWidgetExtension ext) ext.rfu$SetScroll(scroll);
        }
    }
}