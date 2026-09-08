package cloud.glitchdev.rfu.mixin.elementa;

import cloud.glitchdev.rfu.config.categories.OtherSettings;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.constraints.animation.AnimatingConstraints;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = UIComponent.class, remap = false)
public abstract class UIComponentMixin {
    @Final
    @Shadow private List<Function1<AnimatingConstraints, Unit>> beforeHideAnimations;
    @Final
    @Shadow private List<Function1<AnimatingConstraints, Unit>> afterUnhideAnimations;
    @Shadow private int indexInParent;

    @Inject(method = "hide(Z)V", at = @At("HEAD"), cancellable = true)
    private void rfu$onHide(boolean instantly, CallbackInfo ci) {
        if (!OtherSettings.INSTANCE.getPatchElementaMemoryLeaks()) {
            return;
        }

        UIComponent self = (UIComponent) (Object) this;
        if (!self.getHasParent()) {
            ci.cancel();
            return;
        }

        UIComponent parent = self.getParent();
        if (parent == null || !parent.getChildren().contains(self)) {
            ci.cancel();
            return;
        }

        if (instantly || (this.beforeHideAnimations != null && this.beforeHideAnimations.isEmpty())) {
            this.indexInParent = parent.getChildren().indexOf(self);
            parent.removeChild(self);
            ci.cancel();
        }
    }

    @Inject(method = "unhide(Z)V", at = @At("HEAD"), cancellable = true)
    private void rfu$onUnhide(boolean useLastPosition, CallbackInfo ci) {
        if (!OtherSettings.INSTANCE.getPatchElementaMemoryLeaks()) {
            return;
        }

        UIComponent self = (UIComponent) (Object) this;
        if (!self.getHasParent()) {
            ci.cancel();
            return;
        }

        UIComponent parent = self.getParent();
        if (parent == null || parent.getChildren().contains(self)) {
            ci.cancel();
            return;
        }

        if (this.afterUnhideAnimations != null && this.afterUnhideAnimations.isEmpty()) {
            if (useLastPosition && this.indexInParent >= 0 && this.indexInParent < parent.getChildren().size()) {
                parent.getChildren().add(this.indexInParent, self);
            } else {
                parent.getChildren().add(self);
            }
            ci.cancel();
        }
    }
}
