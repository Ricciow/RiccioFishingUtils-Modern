package cloud.glitchdev.rfu.feature.drops

import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.constants.text.TextEffects.*
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.manager.drops.DropManager
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.StringSuggestionProvider
import cloud.glitchdev.rfu.utils.dsl.toFormattedDate
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.minecraft.text.Text

@RFUFeature
object DropsHistory : Feature {
    override fun onInitialize() {
        Command.registerCommand(
            literal("rfudrophistory")
                .executes { context ->
                    context.source.sendFeedback(allDropsMessage())
                    1
                }
                .then(
                    argument("dropName", StringArgumentType.greedyString())
                        .suggests(StringSuggestionProvider(RareDrops.entries.map { it.toString() }))
                        .executes { context ->
                            val dropName = StringArgumentType.getString(context, "dropName")
                            val drop = RareDrops.getRelatedDrop(dropName)
                            var message : Text = TextUtils.rfuLiteral("Drop $dropName doesnt exist!", TextStyle(RED))
                            if (drop != null) {
                                message = singleDropMessage(drop)
                            }
                            context.source.sendFeedback(message)
                            1
                        }
                )
        )
    }

    private fun allDropsMessage() : Text {
        val text = TextUtils.rfuLiteral("Drop History:", TextStyle(GOLD))

        val drops = DropManager.dropHistory.drops

        if(drops.isEmpty()) {
            return text.append(Text.literal("\n $LIGHT_RED${BOLD}No drops :("))
        }

        drops.forEach { dropEntry ->
            val itemName = dropEntry.type.toString()
            val lastDrop = dropEntry.history.lastOrNull() ?: return@forEach
            text.append(Text.literal("\n $YELLOW$BOLD- $itemName: ${YELLOW}Total: $WHITE${dropEntry.history.size} ${YELLOW}- Last: $WHITE${lastDrop.date.toFormattedDate()} (${lastDrop.sinceCount}) $AQUAMARINE(${lastDrop.magicFind}% ✯)"))
        }

        return text
    }

    private fun singleDropMessage(drop : RareDrops) : Text {
        val dropObj = DropManager.dropHistory.getOrAdd(drop)
        val dropHistory = dropObj.history

        val text = TextUtils.rfuLiteral("$drop History:", TextStyle(GOLD))


        if(dropHistory.isEmpty()) {
            return text.append(Text.literal("\n $LIGHT_RED${BOLD}No drops :("))
        }

        text.append(Text.literal("\n $YELLOW${BOLD}Total: $WHITE${dropHistory.size}"))

        dropHistory.forEach { drop ->
            text.append(Text.literal("\n $YELLOW$BOLD- $YELLOW${drop.date.toFormattedDate()}$YELLOW: $WHITE${drop.sinceCount} $AQUAMARINE(${drop.magicFind}% ✯)"))
        }

        return text
    }
}