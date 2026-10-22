package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.WorldBorderRenderEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.WorldBorderRenderer;
//? if >=26.2 {
import net.minecraft.client.renderer.extract.LevelExtractor;
//?} else {
/*import net.minecraft.client.renderer.LevelRenderer;
*///?}
import net.minecraft.client.renderer.state.level.WorldBorderRenderState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >=26.2 {
@Mixin(LevelExtractor.class)
//?} else {
/*@Mixin(LevelRenderer.class)
*///?}
public class WorldBorderExtractionMixin {
    //? if >=26.2 {
    @WrapOperation(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldBorderRenderer;extract(Lnet/minecraft/world/level/border/WorldBorder;FLnet/minecraft/world/phys/Vec3;DLnet/minecraft/client/renderer/state/level/WorldBorderRenderState;)V"))
    //?} else {
    /*@WrapOperation(method = "extractLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldBorderRenderer;extract(Lnet/minecraft/world/level/border/WorldBorder;FLnet/minecraft/world/phys/Vec3;DLnet/minecraft/client/renderer/state/level/WorldBorderRenderState;)V"))
    *///?}
    private void rfu$worldBorder(WorldBorderRenderer renderer, WorldBorder border, float deltaPartialTick, Vec3 cameraPos, double renderDistance, WorldBorderRenderState state, Operation<Void> original) {
        original.call(renderer, border, deltaPartialTick, cameraPos, renderDistance, state);
        WorldBorderRenderEvents.WorldBorderRenderContext context = new WorldBorderRenderEvents.WorldBorderRenderContext(
                border, deltaPartialTick, cameraPos, renderDistance, state);
        WorldBorderRenderEvents.INSTANCE.getRunTasks().invoke(context);
    }
}
