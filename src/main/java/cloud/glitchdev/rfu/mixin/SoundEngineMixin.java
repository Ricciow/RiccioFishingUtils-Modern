package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.utils.BoostedSoundInstance;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.SOFTGainClampEx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @WrapOperation(
        method = {"play", "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F")
    )
    private float rfu$boostVolume(SoundEngine engine, float volume, SoundSource source, Operation<Float> original, @Local(argsOnly = true, name = "instance") SoundInstance instance) {
        float baseVolume = original.call(engine, volume, source);
        if (instance instanceof BoostedSoundInstance boosted) {
            return baseVolume * boosted.getVolumeBoost();
        }
        return baseVolume;
    }

    @WrapOperation(
        method = "play",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V")
    )
    private void rfu$allowAmplification(ChannelAccess.ChannelHandle handle, Consumer<Channel> action, Operation<Void> original, @Local(argsOnly = true, name = "instance") SoundInstance instance) {
        if (instance instanceof BoostedSoundInstance boosted && boosted.getVolumeBoost() > 1f) {
            original.call(handle, (Consumer<Channel>) channel -> {
                if (AL.getCapabilities().AL_SOFT_gain_clamp_ex) {
                    float maxGain = Math.min(boosted.getVolumeBoost(), AL10.alGetFloat(SOFTGainClampEx.AL_GAIN_LIMIT_SOFT));
                    AL10.alSourcef(((ChannelAccessor) channel).rfu$getSource(), AL10.AL_MAX_GAIN, maxGain);
                }
                action.accept(channel);
            });
        } else {
            original.call(handle, action);
        }
    }
}
