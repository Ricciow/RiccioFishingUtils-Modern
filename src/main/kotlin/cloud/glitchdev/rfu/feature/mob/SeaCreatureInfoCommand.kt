package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils.rfuLiteral
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.arguments.StringListArgumentType
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlin.time.Clock

@Command
object SeaCreatureInfoCommand : AbstractCommand("rfusc") {
    override val description: String = "Shows catch data about a specific sea creature"

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            arg("creature", StringListArgumentType(SeaCreatures.entries.map { it.scName }, greedy = true))
                .executes { context ->
                    execute(context)
                }
        )
    }

    private fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        val scName = StringArgumentType.getString(context, "creature")
        val sc = SeaCreatures.entries.find { it.scName.equals(scName, ignoreCase = true) }

        if (sc == null) {
            Chat.sendMessage(rfuLiteral("Sea creature not found: $scName", TextColor.RED))
            return 0
        }

        val record = CatchTracker.catchHistory.getOrAdd(sc)

        val text = rfuLiteral("Catch Data: ", TextStyle(TextColor.GOLD))
            .append("${TextColor.WHITE}${sc.scName}")
            .append("\n${TextColor.YELLOW}Total Catches: ${TextColor.WHITE}${record.total}")
            .append("\n${TextColor.YELLOW}Current Dry Streak: ${TextColor.WHITE}${record.count}")
        
        if (record.total > 0) {
            val ago = Clock.System.now() - record.time
            text.append("\n${TextColor.YELLOW}Last Caught: ${TextColor.WHITE}${ago.toReadableString()} ago")
        }

        if (record.history.isNotEmpty()) {
            val avg = record.history.average()
            val median = record.history.sorted().let {
                if (it.size % 2 == 0) (it[it.size / 2] + it[(it.size / 2) - 1]) / 2.0
                else it[it.size / 2].toDouble()
            }
            val chance = if (avg > 0) (1.0 / avg) * 100 else 0.0

            text.append("\n${TextColor.YELLOW}Average Gap: ${TextColor.WHITE}${String.format("%.1f", avg)}")
            text.append("\n${TextColor.YELLOW}Median Gap: ${TextColor.WHITE}${String.format("%.1f", median)}")
            text.append("\n${TextColor.YELLOW}Est. Chance: ${TextColor.WHITE}${String.format("%.2f%%", chance)}")
            
            val historyStr = record.history.takeLast(10).joinToString(", ")
            text.append("\n${TextColor.YELLOW}Recent History (last 10): ${TextColor.WHITE}$historyStr")
        }

        Chat.sendMessage(text)
        return 1
    }
}
