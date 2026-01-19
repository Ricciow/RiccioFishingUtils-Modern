package cloud.glitchdev.rfu.mixin;

import com.teamresourceful.resourcefulconfig.client.ConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ConfigScreen.class)
public interface ConfigScreenAccessor {
    @Invoker("clearAndInit")
    void invokeClearAndInit();
}
