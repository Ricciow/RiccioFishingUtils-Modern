package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.manager.other.OtherManager
import cloud.glitchdev.rfu.manager.other.data.CakesEntry
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@RFUFeature
object CakeExpiredAlert : Feature {
    val CAKE_EATEN_REGEX = """(?:Big )?Yum! You (?:gain|refresh) \+\d+. (.+) for 48 hours!""".toRegex()
    private val lastOutdated : HashSet<CakesEntry.Cake> = hashSetOf()

    override fun onInitialize() {
        val cakes = OtherManager.getField("cakes") {
            CakesEntry()
        } as? CakesEntry ?: CakesEntry()

        registerTickEvent(interval = 300) {
            if(!OtherSettings.outdatedCake) return@registerTickEvent
            if(!World.isInSkyblock) return@registerTickEvent
            val outdated = cakes.getOutdatedCakes().toHashSet()

            val newOutDated = lastOutdated.minus(outdated)
            if(newOutDated.isNotEmpty()) {
                val message = TextUtils.rfuLiteral("${newOutDated.size} ${TextColor.GOLD}of your cakes just expired!", TextColor.YELLOW)
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
                val message = TextUtils.rfuLiteral("You have ${TextColor.YELLOW}${outdated.size} ${TextColor.GOLD}expired cakes!", TextColor.GOLD)
                Chat.sendMessage(message)
            }
        }

        registerGameEvent(CAKE_EATEN_REGEX) { _, _, match ->
            val effect = match?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            cakes.eatCake(effect)
        }
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