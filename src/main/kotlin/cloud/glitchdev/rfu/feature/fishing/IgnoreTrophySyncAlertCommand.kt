package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.constants.fishing.TrophyType
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.data.fishing.TrophyDataManager
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style

@Command
object IgnoreTrophySyncAlertCommand : AbstractCommand("rfuignoretrophysync") {
    override val description = "Disables a trophy count or pity sync reminder."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        for (section in listOf("count", "pity")) {
            val subCommand = lit(section)
            for (type in TrophyType.entries) {
                subCommand.then(lit(type.name.lowercase()).executes { context ->
                    if (section == "count") {
                        val data = TrophyDataManager.data.count
                        when (type) {
                            TrophyType.FROG -> data.hasSyncedFrogs = true
                            TrophyType.FISH -> data.hasSyncedFish = true
                        }
                    } else {
                        val data = TrophyDataManager.data.pity
                        when (type) {
                            TrophyType.FROG -> data.hasSyncedFrogs = true
                            TrophyType.FISH -> data.hasSyncedFish = true
                        }
                    }
                    TrophyDataManager.save()
                    context.source.sendFeedback(TextUtils.rfuLiteral(
                        "Trophy ${type.name.lowercase()} $section sync reminder disabled.",
                        TextColor.LIGHT_GREEN
                    ))
                    1
                })
            }
            builder.then(subCommand)
        }
    }

    fun button(section: String, type: TrophyType): Component =
        Component.literal("${TextColor.LIGHT_RED}${TextEffects.BOLD}[Ignore]").setStyle(
            Style.EMPTY
                .withClickEvent(ClickEvent.RunCommand("/rfuignoretrophysync $section ${type.name.lowercase()}"))
                .withHoverEvent(HoverEvent.ShowText(Component.literal("Click to disable this sync reminder")))
        )
}
