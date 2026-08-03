package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.utils.gui.HeartsUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//~ if >=26.2 'Gui' -> 'Hud'{
import net.minecraft.client.gui.Hud;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
//~}
public class GuiMixin {
    @WrapOperation(
        method = "extractHearts",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelData;isHardcore()Z")
    )
    private boolean rfu$wrapIsHardcore(LevelData instance, Operation<Boolean> original) {
        return HeartsUtil.INSTANCE.getForceHardcoreHearts() || original.call(instance);
    }
}
