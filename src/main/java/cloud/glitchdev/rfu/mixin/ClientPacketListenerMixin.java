package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.*;
import cloud.glitchdev.rfu.events.wrappers.VoidCancelable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow private ClientLevel level;

    @Inject(method = "handleAddEntity", at = @At("TAIL"))
    private void onEntityAdded(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        Entity entity = this.level.getEntity(packet.getId());
        if (entity != null) {
            EntityAddedEvents.INSTANCE.getRunTasks().invoke(entity);
        }
    }

    @Inject(method = "handleSetEntityData", at = @At("TAIL"))
    private void onEntityData(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        Entity entity = this.level.getEntity(packet.id());
        if (entity != null) {
            EntityDataEvents.INSTANCE.getRunTasks().invoke(entity);
        }
    }

    @Inject(method = "handleContainerContent", at = @At("HEAD"))
    private void onContainerContentPacket(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        ContainerEvents.INSTANCE.getRunTasks().invoke(packet.containerId(), packet.items());
    }

    @Inject(method = "handleRemoveEntities", at = @At("HEAD"))
    private void onEntitiesRemoved(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci) {
        for (int entityId : packet.getEntityIds()) {
            EntityRemovedEvents.INSTANCE.getRunTasks().invoke(entityId);
        }
    }

    @Inject(method = "handleContainerSetSlot", at = @At("HEAD"))
    private void handleContainerSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        SetSlotEvents.INSTANCE.getRunTasks().invoke(packet.getContainerId(), packet.getSlot(), packet.getItem());
    }

    @Inject(method = "handleParticleEvent", at = @At("HEAD"), cancellable = true)
    private void handleLevelParticles(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
        VoidCancelable cancelable = new VoidCancelable(ci);
        ParticleEvents.INSTANCE.getRunTasks().invoke(packet, cancelable);
    }

    @Inject(method = "handleEntityEvent", at = @At("TAIL"))
    private void onEntityEvent(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        Entity entity = packet.getEntity(this.level);
        if (entity != null) {
            EntityStatusEvents.INSTANCE.getRunTasks().invoke(entity, packet.getEventId());
        }
    }
}
