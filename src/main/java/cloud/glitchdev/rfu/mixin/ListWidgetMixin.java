package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.access.ListWidgetExtension;
import com.teamresourceful.resourcefulconfig.client.components.base.ListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ListWidget.class)
public abstract class ListWidgetMixin implements ListWidgetExtension {

    @Shadow private double scroll;

    @Shadow protected abstract void updateScrollBar();

    @Override
    @Unique
    public double rfu$GetScroll() {
        return this.scroll;
    }

    @Override
    @Unique
    public void rfu$SetScroll(double newScroll) {
        this.scroll = newScroll;
        this.updateScrollBar();
    }
}