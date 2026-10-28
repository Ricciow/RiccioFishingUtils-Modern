package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAnyChatEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.beacon
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.cube
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.playerHead
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.text
import gg.essential.universal.utils.toUnformattedString
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents
import net.minecraft.client.renderer.PlayerSkinRenderCache
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.awt.Color
import java.math.BigDecimal
import java.util.function.Supplier
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@RFUFeature
object ChatCoordinates : Feature {
    private val MINIMUM_VISIBLE = 3.seconds
    private val MAXIMUM_VISIBLE = 30.seconds
    private const val ARRIVAL_DISTANCE_SQUARED = 25.0
    private const val NUMBER = """[+-]?(?:\d+(?:\.\d*)?|\.\d+)"""
    private const val SEPARATOR = """(?:\s*,\s+|\s*[;|/]\s*|\s+)"""
    private const val NUMBER_END = """(?![\w.+-]|,\d)"""
    private const val AXIS = """([xyz])\s*[:=]?\s*($NUMBER)$NUMBER_END"""

    private val senderRegex = Regex(
        """^(?:(?:Party|Guild|Officer|Co-op)\s*>\s*|From\s+)?(?:\[[^\]\r\n]+]\s*|[^\x00-\x7F\s]+\s+)*([a-zA-Z0-9_]{3,16})(?:\s+\[[^\]\r\n]+])?\s*:\s*(.+)$"""
    )
    private val labelledRegex = Regex(
        """(?<![\w.+-])$AXIS$SEPARATOR$AXIS$SEPARATOR$AXIS""", RegexOption.IGNORE_CASE
    )
    private val plainRegex = Regex(
        """(?<![\w.+,-])($NUMBER)$NUMBER_END$SEPARATOR($NUMBER)$NUMBER_END$SEPARATOR($NUMBER)$NUMBER_END"""
    )
    private val markerColor = Color(85, 85, 255)
    private val fillColor = Color(85, 85, 255, 55)

    data class Coordinates(val x: Double, val y: Double, val z: Double)
    data class Message(val sender: String, val coordinates: Coordinates)

    private data class Marker(
        val sender: String,
        val position: Vec3,
        val receivedAt: TimeMark,
        val skin: Supplier<PlayerSkinRenderCache.RenderInfo>
    ) {
        val renderPosition = position.add(0.5, 0.5, 0.5)
        val coordinatesText = listOf("X" to position.x, "Y" to position.y, "Z" to position.z)
            .joinToString { (axis, value) -> "$axis: ${BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()}" }
        val label = "${TextColor.LIGHT_BLUE}$sender\n${TextColor.YELLOW}$coordinatesText"
    }

    private val markers = mutableMapOf<Pair<String, Coordinates>, Marker>()

    override fun onInitialize() {
        registerAnyChatEvent { text, _ ->
            if (!OtherSettings.renderChatCoords || mc.level == null) return@registerAnyChatEvent
            val message = parse(text.toUnformattedString()) ?: return@registerAnyChatEvent
            val (x, y, z) = message.coordinates
            val profile = mc.connection?.getPlayerInfo(message.sender)?.profile
            val resolvableProfile = profile?.let { ResolvableProfile.createResolved(it) }
                ?: ResolvableProfile.createUnresolved(message.sender)
            val marker = Marker(
                message.sender, Vec3(x, y, z), TimeSource.Monotonic.markNow(),
                mc.playerSkinRenderCache().createLookup(resolvableProfile)
            )
            markers[message.sender.lowercase() to message.coordinates] = marker
            Chat.sendMessage(TextUtils.rfuLiteral(
                "Rendered coordinates from ${TextColor.LIGHT_BLUE}${marker.sender}${TextColor.YELLOW}",
                TextColor.YELLOW
            ))
        }

        registerTickEvent {
            if (!OtherSettings.renderChatCoords) {
                markers.clear()
                return@registerTickEvent
            }
            val player = mc.player ?: return@registerTickEvent
            markers.values.removeIf { marker ->
                val age = marker.receivedAt.elapsedNow()
                age >= MAXIMUM_VISIBLE ||
                    (age >= MINIMUM_VISIBLE && player.distanceToSqr(marker.renderPosition) <= ARRIVAL_DISTANCE_SQUARED)
            }
        }

        registerRenderEvent { context ->
            if (!OtherSettings.renderChatCoords || mc.level == null) return@registerRenderEvent
            for (marker in markers.values) {
                val playerInfo = mc.connection?.getPlayerInfo(marker.sender)
                val playerSkin = playerInfo?.skin ?: marker.skin.get().playerSkin()
                val labelPosition = marker.renderPosition.add(0.0, 1.5, 0.0)
                val labelScale = 0.05f * maxOf(1f, labelPosition.distanceTo(Render3D.camera.position()).toFloat() / 10f)
                val headOffset = Vector3f(0f, mc.font.lineHeight * 1.5f * labelScale, 0f).rotate(Render3D.camera.rotation())
                Render3D.draw(context) {
                    cube {
                        position = marker.renderPosition
                        color = fillColor
                        filled = true
                        borderColor = markerColor
                    }
                    beacon {
                        position = marker.renderPosition
                        color = markerColor
                    }
                    playerHead {
                        position = labelPosition.add(headOffset.x.toDouble(), headOffset.y.toDouble(), headOffset.z.toDouble())
                        skin = playerSkin.body().texturePath()
                        size = 12f
                        scale = labelScale
                        showHat = playerInfo?.showHat() ?: true
                        seeThrough = true
                    }
                    text {
                        position = labelPosition
                        text = marker.label
                        color = Color.WHITE
                        scale = labelScale
                        backgroundOpacity = 0.75f
                        seeThrough = true
                    }
                }
            }
        }

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register { _, _ -> markers.clear() }
        registerDisconnectEvent { markers.clear() }
    }

    /** Extracts coordinates from the message body, never from ranks or usernames. */
    fun parse(text: String): Message? {
        val senderMatch = senderRegex.matchEntire(text.trim()) ?: return null
        val body = senderMatch.groupValues[2]
        // Prefer explicit axes even when an unrelated numeric triple occurs earlier in the message.
        val labelled = labelledRegex.findAll(body).firstOrNull { match ->
            listOf(1, 3, 5).map { match.groupValues[it].lowercase() }.toSet().size == 3
        }
        val values = if (labelled != null) {
            val axes = listOf(1, 3, 5).associate { index ->
                labelled.groupValues[index].lowercase() to labelled.groupValues[index + 1]
            }
            listOf(axes.getValue("x"), axes.getValue("y"), axes.getValue("z"))
        } else {
            plainRegex.find(body)?.groupValues?.drop(1) ?: return null
        }
        val numbers = values.map { it.toDoubleOrNull() ?: return null }
        if (numbers.any { !it.isFinite() || abs(it) > 30_000_000.0 }) return null
        return Message(senderMatch.groupValues[1], Coordinates(numbers[0], numbers[1], numbers[2]))
    }
}
