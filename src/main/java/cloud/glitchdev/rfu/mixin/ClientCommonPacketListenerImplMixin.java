package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.utils.ResourcePackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {
    @Shadow @Final protected Minecraft minecraft;
    @Shadow @Final protected Connection connection;

    @Inject(method = "handleResourcePackPush", at = @At("HEAD"), cancellable = true)
    private void onHandleResourcePackPush(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        if (ResourcePackUtils.onHandleResourcePackPush(
                this.minecraft,
                this.connection,
                packet.id(),
                packet.hash(),
                packet.url()
        )) {
            ci.cancel();
        }
    }
}
