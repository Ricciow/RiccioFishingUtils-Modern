package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.CakesEntry
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Tablist
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import cloud.glitchdev.rfu.utils.dsl.removeFormatting
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@RFUFeature
object CakeExpiredAlert : Feature {
    val CAKE_EATEN_REGEX = """(?:Big )?Yum! You (?:gain|refresh) \+\d+. (.+) for 48 hours!""".toRegex()
    val CENTURY_CAKES_REGEX = """Century Cakes:\s*(\d+[hms])\s*\((\d+)/(\d+)\)""".toRegex()
    private val CENTURY_CAKES_INACTIVE_REGEX = """Century Cakes:\s*INACTIVE\b""".toRegex()
    private val INACTIVE_GRACE_PERIOD = 5.seconds
    private var inactiveSince: TimeMark? = null
    private val lastOutdated : HashSet<CakesEntry.Cake> = hashSetOf()

    override fun onInitialize() {
        val cakes = OtherManager.getField("cakes") {
            CakesEntry()
        } as? CakesEntry ?: CakesEntry()

        registerJoinEvent { inactiveSince = null }
        registerDisconnectEvent { inactiveSince = null }

        registerTickEvent(interval = 20) {
            if (!OtherSettings.outdatedCake || !World.isInSkyblock) {
                inactiveSince = null
                return@registerTickEvent
            }

            checkTablist(cakes)
        }

        registerTickEvent(interval = 300) {
            if(!OtherSettings.outdatedCake) return@registerTickEvent
            if(!World.isInSkyblock) return@registerTickEvent
            val outdated = cakes.getOutdatedCakes().toHashSet()

            val newOutDated = outdated.minus(lastOutdated)
            if(newOutDated.isNotEmpty()) {
                val hoverText = buildHoverText(newOutDated)
                val message = TextUtils.rfuLiteral("${newOutDated.size} ${TextColor.GOLD}of your cakes just expired!", TextColor.YELLOW)
                    .setStyle(
                        Style.EMPTY
                        .withHoverEvent(HoverEvent.ShowText(Component.literal(hoverText)))
                        .withClickEvent(ClickEvent.RunCommand("visit ricciow"))
                    )
                Chat.sendMessage(message)
            }

            lastOutdated.clear()
            lastOutdated.addAll(outdated)
        }

        registerTickEvent(interval = 6000) {
            if(!OtherSettings.outdatedCake) return@registerTickEvent
            if(!World.isInSkyblock) return@registerTickEvent
            val outdated = cakes.getOutdatedCakes()

            if(outdated.isNotEmpty()) {
                val hoverText = buildHoverText(outdated)
                val message = TextUtils.rfuLiteral("You have ${TextColor.YELLOW}${outdated.size} ${TextColor.GOLD}expired cakes!", TextColor.GOLD)
                    .setStyle(
                        Style.EMPTY
                        .withHoverEvent(HoverEvent.ShowText(Component.literal(hoverText)))
                        .withClickEvent(ClickEvent.RunCommand("visit ricciow"))
                    )
                Chat.sendMessage(message)
            }
        }

        registerGameEvent(CAKE_EATEN_REGEX) { _, _, match ->
            val effect = match?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            inactiveSince = null
            cakes.eatCake(effect)
            OtherManager.file.save()
        }
    }

    private fun checkTablist(cakes: CakesEntry) {
        var inactive = false
        for (line in Tablist.getTablistAsStrings()) {
            val text = line.removeFormatting()
            if (CENTURY_CAKES_INACTIVE_REGEX.containsMatchIn(text)) {
                inactive = true
                break
            }

            val match = CENTURY_CAKES_REGEX.find(text) ?: continue
            val remaining = Duration.parseOrNull(match.groupValues[1]) ?: continue
            val current = match.groupValues[2].toIntOrNull()
            val total = match.groupValues[3].toIntOrNull()
            if (current != null && total != null && total > 0 && current == total && cakes.refreshOutdatedCakes(remaining)) {
                OtherManager.file.save()
                lastOutdated.clear()
                break
            }
        }

        if (!inactive) {
            inactiveSince = null
            return
        }

        val since = inactiveSince ?: TimeSource.Monotonic.markNow().also { inactiveSince = it }
        if (since.elapsedNow() >= INACTIVE_GRACE_PERIOD && cakes.expireAllCakes()) {
            OtherManager.file.save()
        }
    }

    private fun buildHoverText(cakes: Collection<CakesEntry.Cake>): String {
        return "${TextColor.GOLD}Expired Cakes:\n${cakes.joinToString("\n") { "${TextColor.GRAY}- ${TextColor.YELLOW}${it.name}" }}\n${TextColor.DARK_GRAY}If this is incorrect run /rfuclearcakes to reset the cake data.\n${TextColor.GRAY}Click for cakes!"
    }

    @Command
    object ClearCakes : SimpleCommand("rfuclearcakes") {
        override val description: String = "Clears the saved cake timers."

        override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
            val cakes = OtherManager.getField("cakes") {
                CakesEntry()
            } as? CakesEntry ?: CakesEntry()

            cakes.clearCakeList()

            context.source.sendFeedback(TextUtils.rfuLiteral("Successfully cleared cake list.", TextColor.LIGHT_GREEN))

            return 1
        }
    }
}
