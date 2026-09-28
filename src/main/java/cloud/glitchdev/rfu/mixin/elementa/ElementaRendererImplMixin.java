package cloud.glitchdev.rfu.mixin.elementa;

import cloud.glitchdev.rfu.config.categories.OtherSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.essential.elementa.renderer.SpecialRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "gg.essential.elementa.renderer.impl.ElementaRendererImpl", remap = false)
public class ElementaRendererImplMixin {
    @WrapOperation(
        method = "bakeSpecialElements(Ljava/util/List;Lgg/essential/elementa/renderer/SpecialRenderer$Factory;Ljava/util/List;)V",
        at = @At(value = "INVOKE", target = "Lgg/essential/elementa/renderer/SpecialRenderer;getOnlyDrawsInBounds()Z")
    )
    private boolean rfu$separateFontDraws(SpecialRenderer<?> renderer, Operation<Boolean> original) {
        if (OtherSettings.INSTANCE.getPatchElementaTextBleed()
            && renderer.getClass().getName().equals("gg.essential.elementa.FontSpecialRenderer")) {
            return false;
        }
        return original.call(renderer);
    }
}