package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.ContainerEvents;
import cloud.glitchdev.rfu.events.managers.EntityRemovedEvents;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleContainerContent", at = @At("HEAD"))
    private void onContainerContentPacket(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        ContainerEvents.INSTANCE.runTasks(packet.containerId(), packet.items());
    }

    @Inject(method = "handleRemoveEntities", at = @At("HEAD"))
    private void onEntitiesRemoved(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci) {
        for (int entityId : packet.getEntityIds()) {
            EntityRemovedEvents.INSTANCE.runTasks(entityId);
        }
    }
}
