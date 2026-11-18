package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.constants.fishing.HotSpotConstants
import cloud.glitchdev.rfu.constants.fishing.HotspotShareChat
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
import java.util.UUID
import kotlin.math.floor

@RFUFeature
object HotspotSharer : Feature {
    private val notifiedEntityIds = mutableSetOf<Int>()

    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            if (!HotSpotSettings.shareHotspotAlert) return@registerTickEvent

            val player = mc.player ?: return@registerTickEvent
            val channel = HotSpotSettings.hotspotShareChat
            val autoShare = HotSpotSettings.autoShareHotspot && (channel != HotspotShareChat.PARTY || Party.inParty)
            if (autoShare && !FishingSession.isFishing) return@registerTickEvent
            HotSpotEvents.getAllHotspots().forEach { hotspot ->
                val entityId = hotspot.entityId ?: return@forEach
                if (entityId in notifiedEntityIds) return@forEach
                if (hotspot.type == HotspotType.UNKNOWN) return@forEach
                if (player.position().distanceTo(hotspot.center) >= HotSpotConstants.SHARE_DISTANCE) return@forEach

                shareHotspot(hotspot, channel, sendImmediately = autoShare)
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
        val channel = HotSpotSettings.hotspotShareChat
        if (channel == HotspotShareChat.PARTY && !Party.inParty) {
            Chat.sendMessage(TextUtils.rfuLiteral("You must be in a party to share hotspots!", TextColor.LIGHT_RED))
            return
        }

        val player = mc.player ?: return
        val nearestHotspot = HotSpotEvents.getAllHotspots()
            .filter { it.entityId != null && it.type != HotspotType.UNKNOWN }
            .minByOrNull { player.position().distanceTo(it.center) }

        if (nearestHotspot != null && player.position().distanceTo(nearestHotspot.center) < HotSpotConstants.SHARE_DISTANCE) {
            shareHotspot(nearestHotspot, channel, sendImmediately = true)
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

    private fun shareHotspot(hotspot: Hotspot, channel: HotspotShareChat, sendImmediately: Boolean) {
        val entityId = hotspot.entityId ?: return

        val name = formatHotspotShareName(hotspot)
        val pos = hotspot.center
        val command = buildString {
            append(channel.command)
            append(' ')
            append(name)
            append(" Hotspot | ")
            append(floor(pos.x).toInt())
            append(", ")
            append(floor(pos.y).toInt() - 2)
            append(", ")
            append(floor(pos.z).toInt())
            if (channel == HotspotShareChat.ALL_CHAT) {
                append(" <@")
                append(UUID.randomUUID().toString().substringBefore('-'))
                append('>')
            }
        }

        if (sendImmediately) {
            Chat.sendCommand(command)
        } else {
            showShareMessage(hotspot, name, channel, command.takeIf { channel != HotspotShareChat.PARTY || Party.inParty })
        }
        notifiedEntityIds.add(entityId)
    }

    private fun showShareMessage(hotspot: Hotspot, name: String, channel: HotspotShareChat, command: String?) {
        val effectName = Component.literal(name).withStyle(Style.EMPTY.withColor(hotspot.color.rgb))
        val base = TextUtils.rfuLiteral("Near a ", TextColor.CYAN)
            .append(effectName.copy())
            .append(" ${TextColor.CYAN}hotspot!")

        if (command != null) {
            val buttonColor = if (channel == HotspotShareChat.PARTY) TextColor.LIGHT_BLUE else TextColor.YELLOW
            base.append(" ").append(
                Component.literal("$buttonColor${TextEffects.BOLD}[$channel]")
                    .setStyle(
                        Style.EMPTY
                            .withClickEvent(ClickEvent.RunCommand("/$command"))
                            .withHoverEvent(HoverEvent.ShowText(
                                Component.literal("${TextColor.YELLOW}Click to share this ")
                                    .append(effectName.copy())
                                    .append(" ${TextColor.YELLOW}hotspot!")
                            ))
                    )
            )
        }
        Chat.sendMessage(base)
    }
}
