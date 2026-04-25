package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.constants.HotspotType
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.TextUtils
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style

@RFUFeature
object HotspotSharer : Feature {
    private val notifiedHotspots = mutableSetOf<BlockPos>()

    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            if (!HotSpotSettings.shareHotspotAlert) return@registerTickEvent
            if (!Party.inParty) return@registerTickEvent

            val player = mc.player ?: return@registerTickEvent
            HotSpotEvents.getAllHotspots().forEach { hotspot ->
                if (hotspot.type == HotspotType.UNKNOWN) return@forEach
                if (notifiedHotspots.contains(hotspot.blockPos)) return@forEach

                val distance = player.position().distanceTo(hotspot.center)
                if (distance < 10.0) {
                    notifiedHotspots.add(hotspot.blockPos)
                    if (HotSpotSettings.autoShareHotspot) {
                        shareHotspot(hotspot)
                    } else {
                        showShareMessage(hotspot)
                    }
                }
            }
        }

        registerLocationEvent {
            notifiedHotspots.clear()
        }

        registerDisconnectEvent {
            notifiedHotspots.clear()
        }
    }

    private fun shareHotspot(hotspot: Hotspot) {
        if (!Party.inParty) return
        val pos = hotspot.center
        val stat = hotspot.type.displayName
        Chat.sendCommand("pc $stat Hotspot - ${pos.x.toInt()}, ${pos.y.toInt()}, ${pos.z.toInt()}")
    }

    private fun showShareMessage(hotspot: Hotspot) {
        val pos = hotspot.center
        val stat = hotspot.type.displayName
        
        val base = TextUtils.rfuLiteral("Near a ${TextColor.AQUAMARINE}$stat ${TextColor.CYAN}hotspot! ", TextColor.CYAN)
        
        val clickComponent = Component.literal("${TextColor.GOLD}${TextEffects.BOLD}[Share]")
            .setStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand("/pc $stat Hotspot - ${pos.x.toInt()}, ${pos.y.toInt()}, ${pos.z.toInt()}"))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("${TextColor.YELLOW}Click to share this ${TextColor.AQUAMARINE}$stat ${TextColor.YELLOW}hotspot with your party!\n${TextColor.GRAY}Location: ${pos.x.toInt()}, ${pos.y.toInt()}, ${pos.z.toInt()}")))
            )
            
        base.append(clickComponent)
        Chat.sendMessage(base)
    }
}
