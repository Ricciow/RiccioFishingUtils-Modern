package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.config.categories.DevSettings
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
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.TextUtils
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import kotlin.math.ceil
import java.util.UUID

@RFUFeature
object HotspotSharer : Feature {
    private val notifiedHotspots = mutableSetOf<BlockPos>()
    private val lastAlertStates = mutableMapOf<UUID, AlertState>()
    private enum class AlertState {
        OUT_OF_RANGE, UNKNOWN_BUFF, ALREADY_NOTIFIED, AUTO_SHARE, LOCAL_ALERT
    }

    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            val hotspots = HotSpotEvents.getAllHotspots().toList()
            notifiedHotspots.retainAll(hotspots.map { it.blockPos }.toSet())
            lastAlertStates.keys.retainAll(hotspots.map { it.uuid }.toSet())
            if (!HotSpotSettings.shareHotspotAlert) return@registerTickEvent

            val player = mc.player ?: return@registerTickEvent
            hotspots.forEach { hotspot ->
                val distance = player.position().distanceTo(hotspot.center)
                val state = when {
                    distance >= HotSpotConstants.SHARE_DISTANCE -> AlertState.OUT_OF_RANGE
                    hotspot.type == HotspotType.UNKNOWN -> AlertState.UNKNOWN_BUFF
                    hotspot.blockPos in notifiedHotspots -> AlertState.ALREADY_NOTIFIED
                    HotSpotSettings.autoShareHotspot && Party.inParty -> AlertState.AUTO_SHARE
                    else -> AlertState.LOCAL_ALERT
                }
                when (state) {
                    AlertState.AUTO_SHARE -> shareHotspot(hotspot)
                    AlertState.LOCAL_ALERT -> showShareMessage(hotspot)
                    else -> return@forEach
                }
                notifiedHotspots.add(hotspot.blockPos)
            }
        }

        HotSpotEvents.registerHotSpotDisposeEvent { hotspot ->
            if (HotSpotEvents.getAllHotspots().none { it.blockPos == hotspot.blockPos }) {
                notifiedHotspots.remove(hotspot.blockPos)
            }
            lastAlertStates.remove(hotspot.uuid)
        }

        registerLocationEvent {
            notifiedHotspots.clear()
            lastAlertStates.clear()
        }

        registerDisconnectEvent {
            notifiedHotspots.clear()
            lastAlertStates.clear()
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
            .filter { it.type != HotspotType.UNKNOWN }
            .minByOrNull { player.position().distanceTo(it.center) }

        if (nearestHotspot != null && player.position().distanceTo(nearestHotspot.center) < HotSpotConstants.SHARE_DISTANCE) {
            shareHotspot(nearestHotspot)
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

    private fun shareHotspot(hotspot: Hotspot) {
        if (!Party.inParty) return
        val pos = hotspot.center
        val name = formatHotspotShareName(hotspot)
        Chat.sendCommand("pc $name Hotspot | ${ceil(pos.x).toInt()}, ${ceil(pos.y).toInt()}, ${ceil(pos.z).toInt()}")
    }

    private fun showShareMessage(hotspot: Hotspot) {
        val pos = hotspot.center
        val name = formatHotspotShareName(hotspot)
        val location = "${ceil(pos.x).toInt()}, ${ceil(pos.y).toInt()}, ${ceil(pos.z).toInt()}"
        val shareMessage = "$name Hotspot | $location"
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
