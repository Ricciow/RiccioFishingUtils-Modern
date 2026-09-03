package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.PacketSentEvents;
import cloud.glitchdev.rfu.events.wrappers.VoidCancelable;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Shadow public abstract PacketFlow getSending();

    @Inject(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSendPacket(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        if (this.getSending() == PacketFlow.SERVERBOUND) {
            VoidCancelable cancelable = new VoidCancelable(ci);
            PacketSentEvents.INSTANCE.getRunTasks().invoke(packet, cancelable);
        }
    }
}
