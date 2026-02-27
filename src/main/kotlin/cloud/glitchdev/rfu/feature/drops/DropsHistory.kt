package cloud.glitchdev.rfu.feature.drops

import cloud.glitchdev.rfu.constants.Dyes
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.constants.text.TextEffects.*
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.manager.drops.DropManager
import cloud.glitchdev.rfu.manager.drops.DropRecord
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.arguments.StringListArgumentType
import cloud.glitchdev.rfu.utils.dsl.toFormattedDate
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

object DropsHistory {
    @Command
    object DropHistoryCommand : AbstractCommand("rfudrophistory") {
        override val description: String = "Sends the latest drop for each item you've dropped or detailed information if specified"

        override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
            builder
                .executes { context ->
                    context.source.sendFeedback(allDropsMessage())
                    1
                }
                .then(
                    arg("dropName", StringListArgumentType(
                        RareDrops.entries.map { it.toString() } + Dyes.entries.map { it.toString() }, true
                    ))
                        .executes { context ->
                            val dropName = StringArgumentType.getString(context, "dropName")
                            val message: Component = RareDrops.getRelatedDrop(dropName)?.let { singleDropMessage(it) }
                                ?: Dyes.getRelatedDye(dropName)?.let { singleDyeDropMessage(it) }
                                ?: TextUtils.rfuLiteral("Drop $dropName doesnt exist!", TextStyle(RED))
                            context.source.sendFeedback(message)
                            1
                        }
                )
        }
    }

    private fun allDropsMessage() : Component {
        val text = TextUtils.rfuLiteral("Drop History:", TextStyle(GOLD))

        val drops = DropManager.dropHistory.drops
        val dyeDrops = DropManager.dropHistory.dyeDrops

        if(drops.isEmpty() && dyeDrops.isEmpty()) {
            return text.append(Component.literal("\n $LIGHT_RED${BOLD}No drops :("))
        }

        drops.forEach { dropEntry ->
            try {
                val itemName = dropEntry.type.toString()
                val lastDrop = dropEntry.history.lastOrNull() ?: return@forEach
                val sincePart = lastDrop.sinceCount?.let { " ($it)" } ?: ""
                text.append(Component.literal("\n $YELLOW$BOLD- $itemName: ${YELLOW}Total: $WHITE${dropEntry.history.size} ${YELLOW}- Last: $WHITE${lastDrop.date.toFormattedDate()}$sincePart $AQUAMARINE(${lastDrop.magicFind}% ✯)"))
            } catch (e : Exception) {
                RFULogger.error("Error on rfudrophistory:", e)
            }
        }

        dyeDrops.forEach { dropEntry ->
            try {
                val itemName = dropEntry.type.toString()
                val lastDrop = dropEntry.history.lastOrNull() ?: return@forEach
                val sincePart = lastDrop.sinceCount?.let { " ($it)" } ?: ""
                text.append(Component.literal("\n $YELLOW$BOLD- $itemName: ${YELLOW}Total: $WHITE${dropEntry.history.size} ${YELLOW}- Last: $WHITE${lastDrop.date.toFormattedDate()}$sincePart $AQUAMARINE(${lastDrop.magicFind}% ✯)"))
            } catch (e : Exception) {
                RFULogger.error("Error on rfudrophistory (dye):", e)
            }
        }

        return text
    }

    private fun singleDropMessage(drop: RareDrops): Component =
        singleDropHistoryMessage(drop.toString(), DropManager.dropHistory.getOrAdd(drop).history)

    private fun singleDyeDropMessage(drop: Dyes): Component =
        singleDropHistoryMessage(drop.toString(), DropManager.dropHistory.getOrAdd(drop).history)

    private fun singleDropHistoryMessage(name: String, history: List<DropRecord>): Component {
        val text = TextUtils.rfuLiteral("$name History:", TextStyle(GOLD))

        if (history.isEmpty()) {
            return text.append(Component.literal("\n $LIGHT_RED${BOLD}No drops :("))
        }

        text.append(Component.literal("\n $YELLOW${BOLD}Total: $WHITE${history.size}"))

        history.forEach { drop ->
            val sincePart = drop.sinceCount?.let { "$WHITE$it " } ?: ""
            text.append(Component.literal("\n $YELLOW$BOLD- $YELLOW${drop.date.toFormattedDate()}$YELLOW: $sincePart$AQUAMARINE(${drop.magicFind}% ✯)"))
        }

        return text
    }
}