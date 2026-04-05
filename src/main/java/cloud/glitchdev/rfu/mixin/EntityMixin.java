package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.access.EntityAccess;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.Color;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityAccess {

    @Unique
    private boolean rfu$isGlowing = false;

    @Unique
    private Color rfu$glowColor = Color.WHITE;

    @Override
    public void rfu$setGlowing(boolean glowing) {
        this.rfu$isGlowing = glowing;
    }

    @Override
    public boolean rfu$isGlowing() {
        return this.rfu$isGlowing;
    }

    @Override
    public void rfu$setGlowColor(Color color) {
        this.rfu$glowColor = color != null ? color : Color.WHITE;
    }

    @Override
    public Color rfu$getGlowColor() {
        return this.rfu$glowColor;
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void rfu$isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (this.rfu$isGlowing) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void rfu$getTeamColor(CallbackInfoReturnable<Integer> cir) {
        if (this.rfu$isGlowing) {
            cir.setReturnValue(this.rfu$glowColor.getRGB() & 0xFFFFFF);
        }
    }
}
