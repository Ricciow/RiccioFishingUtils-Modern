package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.events.managers.PacketSentEvents.registerPacketSentEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.dsl.isFishingRod
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
import net.minecraft.world.item.FishingRodItem

@RFUFeature
object FixFailedCasts : Feature {
    override fun onInitialize() {
        registerPacketSentEvent(priority = 0) { packet, cancelable ->
            if (packet !is ServerboundUseItemOnPacket) return@registerPacketSentEvent
            if (!World.isInSkyblock) return@registerPacketSentEvent
            if (!OtherSettings.fixFailedCasts) return@registerPacketSentEvent

            val player = mc.player ?: return@registerPacketSentEvent
            val itemInHand = player.getItemInHand(packet.hand)
            if (itemInHand.isEmpty) return@registerPacketSentEvent

            val isRod = itemInHand.item is FishingRodItem || itemInHand.isFishingRod()
            if (isRod) {
                cancelable.cancel()
            }
        }
    }
}
