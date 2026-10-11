package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.access.PlayerVisibilityRenderStateAccess;
import cloud.glitchdev.rfu.constants.ui.VisiblePlayerEquipment;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements PlayerVisibilityRenderStateAccess {
    @Unique
    private @Nullable Set<VisiblePlayerEquipment> rfu$visiblePlayerEquipment;

    @Override
    public @Nullable Set<VisiblePlayerEquipment> rfu$visiblePlayerEquipment() {
        return rfu$visiblePlayerEquipment;
    }

    @Override
    public void rfu$setVisiblePlayerEquipment(@Nullable Set<VisiblePlayerEquipment> visible) {
        rfu$visiblePlayerEquipment = visible;
    }
}