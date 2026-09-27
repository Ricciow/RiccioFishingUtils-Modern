package cloud.glitchdev.rfu.mixin.elementa;

import cloud.glitchdev.rfu.config.categories.OtherSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.essential.elementa.VanillaFontRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Temporary workaround for SkyOcean reporting a width smaller than the text it renders. */
@Mixin(value = VanillaFontRenderer.class, remap = false)
public class VanillaFontRendererMixin {
    @WrapOperation(
        method = "getStringWidth(Ljava/lang/String;F)F",
        at = @At(value = "INVOKE", target = "Lgg/essential/universal/UGraphics;getStringWidth(Ljava/lang/String;)I")
    )
    private int rfu$includeRenderedTextWidth(String string, Operation<Integer> original) {
        int width = original.call(string);
        if (!OtherSettings.INSTANCE.getPatchSkyOceanTextWidth() || !FabricLoader.getInstance().isModLoaded("skyocean")) {
            return width;
        }

        ScreenRectangle bounds = Minecraft.getInstance().font.prepareText(string, 0f, 0f, -1, false, 0).bounds();
        return bounds == null ? width : Math.max(width, bounds.right() + 2);
    }
}