package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.access.NearbyPlayerRenderStateAccess;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements NearbyPlayerRenderStateAccess {
    @Unique
    private boolean rfu$hideNearbyPlayer;

    @Override
    public boolean rfu$hideNearbyPlayer() {
        return rfu$hideNearbyPlayer;
    }

    @Override
    public void rfu$setHideNearbyPlayer(boolean hidden) {
        rfu$hideNearbyPlayer = hidden;
    }
}