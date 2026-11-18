package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.constants.fishing.HotSpotConstants
import cloud.glitchdev.rfu.constants.fishing.HotspotType
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.KeybindEvents.registerKeybind
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.TextUtils
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import kotlin.math.ceil
import kotlin.math.floor

@RFUFeature
object HotspotSharer : Feature {
    private val notifiedEntityIds = mutableSetOf<Int>()

    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            if (!HotSpotSettings.shareHotspotAlert) return@registerTickEvent

            val player = mc.player ?: return@registerTickEvent
            HotSpotEvents.getAllHotspots().forEach { hotspot ->
                val entityId = hotspot.entityId ?: return@forEach
                if (entityId in notifiedEntityIds) return@forEach
                if (hotspot.type == HotspotType.UNKNOWN) return@forEach
                if (player.position().distanceTo(hotspot.center) >= HotSpotConstants.SHARE_DISTANCE) return@forEach

                shareHotspot(hotspot, sendToParty = HotSpotSettings.autoShareHotspot && Party.inParty)
            }
        }

        registerLocationEvent {
            notifiedEntityIds.clear()
        }

        registerDisconnectEvent {
            notifiedEntityIds.clear()
        }

        registerKeybind {
            key = { HotSpotSettings.shareHotspotKey }
            onPress = { shareNearestHotspot() }
        }
    }

    private fun shareNearestHotspot() {
        if (!Party.inParty) {
            Chat.sendMessage(TextUtils.rfuLiteral("You must be in a party to share hotspots!", TextColor.LIGHT_RED))
            return
        }

        val player = mc.player ?: return
        val nearestHotspot = HotSpotEvents.getAllHotspots()
            .filter { it.entityId != null && it.type != HotspotType.UNKNOWN }
            .minByOrNull { player.position().distanceTo(it.center) }

        if (nearestHotspot != null && player.position().distanceTo(nearestHotspot.center) < HotSpotConstants.SHARE_DISTANCE) {
            shareHotspot(nearestHotspot, sendToParty = true)
        } else {
            Chat.sendMessage(TextUtils.rfuLiteral("No hotspots nearby to share!", TextColor.LIGHT_RED))
        }
    }

    private fun formatHotspotShareName(hotspot: Hotspot): String {
        val buff = hotspot.buff.trim()
        if (buff.isBlank()) return hotspot.type.displayName

        return if (hotspot.type == HotspotType.SHARD) {
            buff.replace(Regex("(?i)^Chance of\\s+"), "").trim().ifBlank { hotspot.type.displayName }
        } else {
            buff.replace(Regex("""(\+\d+)\.\s*"""), "$1 ").trim().ifBlank { hotspot.type.displayName }
        }
    }

    private fun shareHotspot(hotspot: Hotspot, sendToParty: Boolean) {
        val entityId = hotspot.entityId ?: return
        if (sendToParty && !Party.inParty) return

        val name = formatHotspotShareName(hotspot)
        val pos = hotspot.center
        val shareMessage = buildString {
            append(name)
            append(" Hotspot | ")
            append(floor(pos.x).toInt())
            append(", ")
            append(floor(pos.y).toInt() - 2)
            append(", ")
            append(floor(pos.z).toInt())
        }

        if (sendToParty) {
            Chat.sendCommand("pc $shareMessage")
        } else {
            showShareMessage(hotspot, name, shareMessage)
        }
        notifiedEntityIds.add(entityId)
    }

    private fun showShareMessage(hotspot: Hotspot, name: String, shareMessage: String) {
        val effectName = Component.literal(name).withStyle(Style.EMPTY.withColor(hotspot.color.rgb))
        
        val base = TextUtils.rfuLiteral("Near a ", TextColor.CYAN)
            .append(effectName.copy())
            .append(" ${TextColor.CYAN}hotspot! ")
        
        val clickComponent = Component.literal("${TextColor.LIGHT_BLUE}${TextEffects.BOLD}[Party]")
            .setStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand("/pc $shareMessage"))
                    .withHoverEvent(HoverEvent.ShowText(
                        Component.literal("${TextColor.YELLOW}Click to share this ")
                            .append(effectName.copy())
                            .append(" ${TextColor.YELLOW}hotspot with your party!")
                    ))
            )
            
        if (Party.inParty) {
            base.append(clickComponent)
            base.append(" ")
        }

        base.append(
            Component.literal("${TextColor.YELLOW}${TextEffects.BOLD}[All Chat]")
                .setStyle(
                    Style.EMPTY
                        .withClickEvent(ClickEvent.RunCommand("/ac $shareMessage"))
                        .withHoverEvent(HoverEvent.ShowText(
                            Component.literal("${TextColor.YELLOW}Click to share this ")
                                .append(effectName.copy())
                                .append(" ${TextColor.YELLOW}hotspot in all chat!")
                        ))
                )
        )
        Chat.sendMessage(base)
    }
}
