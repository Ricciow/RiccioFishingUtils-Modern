package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.ScreenEffectEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if >=26.3 {
import net.minecraft.client.renderer.extract.LevelExtractor;

@Mixin(LevelExtractor.class)
//?} else {
/*import net.minecraft.client.renderer.ScreenEffectRenderer;

@Mixin(ScreenEffectRenderer.class)
*///?}
public class ScreenEffectMixin {
    //? if >=26.3 {
    @WrapOperation(method = "extractPlayerState", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;colorFromFloat(FFFF)I"))
    //?} else if >=26.2 {
    /*@WrapOperation(method = "submitWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;colorFromFloat(FFFF)I"))
    *///?} else {
    /*@WrapOperation(method = "renderWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;colorFromFloat(FFFF)I"))
    *///?}
    //? if >=26.3 {
    private int rfu$waterOverlayColor(float alpha, float red, float green, float blue, Operation<Integer> original) {
    //?} else {
    /*private static int rfu$waterOverlayColor(float alpha, float red, float green, float blue, Operation<Integer> original) {
    *///?}
        int color = original.call(alpha, red, green, blue);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return color;
        ScreenEffectEvents.WaterOverlayAppearanceContext context = new ScreenEffectEvents.WaterOverlayAppearanceContext(player);
        ScreenEffectEvents.WaterOverlayAppearanceEventManager.INSTANCE.getRunTasks().invoke(context);
        return context.getColor() != null ? ARGB.multiply(color, context.getColor().getRGB()) : color;
    }

    //? if >=26.3 {
    @WrapOperation(method = "extractPlayerState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    //?} else if >=26.2 {
    /*@WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    *///?} else {
    /*@WrapOperation(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    *///?}
    private boolean rfu$waterOverlay(LocalPlayer player, TagKey<Fluid> fluid, Operation<Boolean> original) {
        ScreenEffectEvents.OverlayContext context = new ScreenEffectEvents.OverlayContext(player, original.call(player, fluid));
        ScreenEffectEvents.WaterOverlayEventManager.INSTANCE.getRunTasks().invoke(context);
        return context.getVisible();
    }

    //? if >=26.3 {
    @WrapOperation(method = "extractPlayerState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"))
    //?} else if >=26.2 {
    /*@WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"))
    *///?} else {
    /*@WrapOperation(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"))
    *///?}
    private boolean rfu$fireOverlay(LocalPlayer player, Operation<Boolean> original) {
        ScreenEffectEvents.OverlayContext context = new ScreenEffectEvents.OverlayContext(player, original.call(player));
        ScreenEffectEvents.FireOverlayEventManager.INSTANCE.getRunTasks().invoke(context);
        return context.getVisible();
    }
}