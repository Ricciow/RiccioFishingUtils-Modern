package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.PacketSentEvents;
import cloud.glitchdev.rfu.events.managers.ParticleEvents;
import cloud.glitchdev.rfu.events.wrappers.VoidCancelable;
import cloud.glitchdev.rfu.feature.other.TpsTracker;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
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

    @Inject(
        method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
        at = @At("HEAD")
    )
    private void onReceivePacket(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientboundPingPacket pingPacket) {
            TpsTracker.INSTANCE.onPing(pingPacket.getId());
        } else if (packet instanceof ClientboundSetTimePacket timePacket) {
            TpsTracker.INSTANCE.onTimePacket(timePacket.gameTime());
        } else if (packet instanceof ClientboundLevelParticlesPacket particlesPacket) {
            ParticleEvents.INSTANCE.processPacket(particlesPacket);
        } else if (packet instanceof ClientboundBundlePacket bundlePacket) {
            for (Packet<?> subPacket : bundlePacket.subPackets()) {
                if (subPacket instanceof ClientboundPingPacket pingPacket) {
                    TpsTracker.INSTANCE.onPing(pingPacket.getId());
                } else if (subPacket instanceof ClientboundSetTimePacket timePacket) {
                    TpsTracker.INSTANCE.onTimePacket(timePacket.gameTime());
                } else if (subPacket instanceof ClientboundLevelParticlesPacket particlesPacket) {
                    ParticleEvents.INSTANCE.processPacket(particlesPacket);
                }
            }
        }
    }
}
