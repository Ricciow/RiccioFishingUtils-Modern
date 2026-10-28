package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.ScreenEffectEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenEffectRenderer.class)
public class WaterOverlayTextureMixin {
    //? if >=26.2 {
    @WrapOperation(method = "submitWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;blockScreenEffect(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    //?} else {
    /*@WrapOperation(method = "renderWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;blockScreenEffect(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    *///?}
    private static RenderType rfu$waterOverlayTexture(Identifier texture, Operation<RenderType> original) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return original.call(texture);
        ScreenEffectEvents.WaterOverlayAppearanceContext context = new ScreenEffectEvents.WaterOverlayAppearanceContext(player);
        ScreenEffectEvents.WaterOverlayAppearanceEventManager.INSTANCE.getRunTasks().invoke(context);
        return original.call(context.getTexture() != null ? context.getTexture() : texture);
    }
}