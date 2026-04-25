package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.utils.MobUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.animal.squid.Squid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Squid.class)
public abstract class SquidMixin {
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void rfu$onAiStep(CallbackInfo ci) {
        Squid squid = (Squid) (Object) this;
        if (squid.level().isClientSide() && MobUtils.INSTANCE.isPlhlegblast(squid)) {
            for (int i = 0; i < 2; ++i) {
                squid.level().addParticle(
                    ParticleTypes.GLOW,
                    squid.getRandomX(0.5),
                    squid.getRandomY() - 0.25,
                    squid.getRandomZ(0.5),
                    (squid.getRandom().nextDouble() - 0.5) * 0.1,
                    (squid.getRandom().nextDouble() - 0.5) * 0.1,
                    (squid.getRandom().nextDouble() - 0.5) * 0.1
                );
            }
        }
    }
}
