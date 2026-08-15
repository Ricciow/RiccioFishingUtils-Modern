package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
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
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent

@RFUFeature
object CakeExpiredAlert : Feature {
    val CAKE_EATEN_REGEX = """(?:Big )?Yum! You (?:gain|refresh) \+\d+. (.+) for 48 hours!""".toRegex()
    val CENTURY_CAKES_REGEX = """Century Cakes:\s*\d+[hms]\s*\((\d+)/(\d+)\)""".toRegex()
    private val lastOutdated : HashSet<CakesEntry.Cake> = hashSetOf()

    override fun onInitialize() {
        val cakes = OtherManager.getField("cakes") {
            CakesEntry()
        } as? CakesEntry ?: CakesEntry()

        registerTickEvent(interval = 20) {
            if(!OtherSettings.outdatedCake) return@registerTickEvent
            if(!World.isInSkyblock) return@registerTickEvent

            checkTablistAndClearIfNeeded(cakes)
        }

        registerTickEvent(interval = 300) {
            if(!OtherSettings.outdatedCake) return@registerTickEvent
            if(!World.isInSkyblock) return@registerTickEvent
            val outdated = cakes.getOutdatedCakes().toHashSet()

            val newOutDated = outdated.minus(lastOutdated)
            if(newOutDated.isNotEmpty()) {
                val hoverText = buildHoverText(newOutDated)
                val message = TextUtils.rfuLiteral("${newOutDated.size} ${TextColor.GOLD}of your cakes just expired!", TextColor.YELLOW)
                    .withStyle { it.withHoverEvent(HoverEvent.ShowText(Component.literal(hoverText))) }
                Chat.sendMessage(message)
            }

            lastOutdated.clear()
            lastOutdated.addAll(outdated)
        }

        registerTickEvent(interval = 3000) {
            if(!OtherSettings.outdatedCake) return@registerTickEvent
            if(!World.isInSkyblock) return@registerTickEvent
            val outdated = cakes.getOutdatedCakes()

            if(outdated.isNotEmpty()) {
                val hoverText = buildHoverText(outdated)
                val message = TextUtils.rfuLiteral("You have ${TextColor.YELLOW}${outdated.size} ${TextColor.GOLD}expired cakes!", TextColor.GOLD)
                    .withStyle { it.withHoverEvent(HoverEvent.ShowText(Component.literal(hoverText))) }
                Chat.sendMessage(message)
            }
        }

        registerGameEvent(CAKE_EATEN_REGEX) { _, _, match ->
            val effect = match?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            cakes.eatCake(effect)
            OtherManager.file.save()
        }
    }

    private fun checkTablistAndClearIfNeeded(cakes: CakesEntry) {
        if (cakes.getOutdatedCakes().isEmpty()) return
        for (line in Tablist.getTablistAsStrings()) {
            val match = CENTURY_CAKES_REGEX.find(line.removeFormatting()) ?: continue
            val current = match.groupValues[1].toIntOrNull()
            val total = match.groupValues[2].toIntOrNull()
            if (current != null && total != null && current == total) {
                cakes.clearCakeList()
                OtherManager.file.save()
                lastOutdated.clear()
                break
            }
        }
    }

    private fun buildHoverText(cakes: Collection<CakesEntry.Cake>): String {
        return "${TextColor.GOLD}Expired Cakes:\n${cakes.joinToString("\n") { "${TextColor.GRAY}- ${TextColor.YELLOW}${it.name}" }}\n${TextColor.DARK_GRAY}If this is incorrect run /rfuclearcakes to reset the cake data."
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