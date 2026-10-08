package cloud.glitchdev.rfu.mixin.universalcraft;

import cloud.glitchdev.rfu.utils.TextUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.essential.universal.UGraphics;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Keeps Elementa's text sizing and rendering constraints on the same measurement path. */
@Mixin(value = UGraphics.class, remap = false)
public class UGraphicsMixin {
    @WrapOperation(
        method = "getStringWidth(Ljava/lang/String;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I")
    )
    private static int rfu$measureStyledTextWidth(Font font, String str, Operation<Integer> original) {
        return TextUtils.patchSkyOceanTextWidth(font, str, original.call(font, str));
    }
}
