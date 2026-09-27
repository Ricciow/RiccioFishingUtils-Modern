package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.arguments.StringListArgumentType
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import kotlin.time.Duration.Companion.milliseconds

@Command
object PersonalBestCommand : AbstractCommand("rfupb") {
    private const val PAGE_SIZE = 10

    override val description: String = "Shows saved sea creature personal best kill times."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes {
            showPage(1)
        }

        builder.then(
            arg("name", StringListArgumentType(SeaCreatures.entries.flatMap { listOf(it.scName, it.scDisplayName) }.distinct(), greedy = true, exclusive = false))
                .executes { context ->
                    val name = StringArgumentType.getString(context, "name").trim()
                    name.toIntOrNull()?.let { return@executes showPage(it) }

                    val sc = SeaCreatures.entries.find {
                        it.scName.equals(name, ignoreCase = true) ||
                            it.scDisplayName.equals(name, ignoreCase = true)
                    }

                    if (sc == null) {
                        Chat.sendMessage(TextUtils.rfuLiteral("Sea creature not found: $name", TextColor.LIGHT_RED))
                        return@executes 0
                    }

                    val best = CatchTracker.catchHistory.catches.find { it.name == sc.scName }?.bestKillTimeMs
                    if (best == null) {
                        Chat.sendMessage(TextUtils.rfuLiteral("No personal best kill time saved for ${sc.scDisplayName}.", TextColor.LIGHT_RED))
                        return@executes 0
                    }

                    Chat.sendMessage(TextUtils.rfuLiteral("${TextColor.YELLOW}${sc.scDisplayName} ${TextColor.GOLD}Personal Best: ${TextColor.YELLOW}${best.milliseconds.toReadableString(true)}"))
                    1
                }
        )
    }

    private fun showPage(page: Int): Int {
        val records = CatchTracker.catchHistory.catches
            .mapNotNull { record -> record.bestKillTimeMs?.let { record to it } }
            .sortedBy { it.first.name }

        if (records.isEmpty()) {
            Chat.sendMessage(TextUtils.rfuLiteral("No personal best kill times saved.", TextColor.LIGHT_RED))
            return 0
        }

        val totalPages = (records.size + PAGE_SIZE - 1) / PAGE_SIZE
        val currentPage = page.coerceIn(1, totalPages)
        val startIndex = (currentPage - 1) * PAGE_SIZE
        val pageItems = records.subList(startIndex, minOf(startIndex + PAGE_SIZE, records.size))
        val message = TextUtils.rfuLiteral("${TextColor.GOLD}Personal Best Kill Times:")

        pageItems.forEachIndexed { index, (record, best) ->
            val name = SeaCreatures.get(record.name)?.scDisplayName ?: record.name
            message.append(Component.literal("\n ${TextColor.GRAY}${startIndex + index + 1} - ${TextColor.YELLOW}$name: ${TextColor.WHITE}${best.milliseconds.toReadableString(true)}"))
        }

        message.append(buildFooter(currentPage, totalPages))
        Chat.sendMessage(message)
        return 1
    }

    private fun buildFooter(currentPage: Int, totalPages: Int): Component {
        val previous = if (currentPage > 1) {
            Component.literal("${TextColor.GOLD}<<").setStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand("/rfupb ${currentPage - 1}"))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("Previous page")))
            )
        } else {
            Component.literal("${TextColor.DARK_GRAY}<<")
        }

        val next = if (currentPage < totalPages) {
            Component.literal("${TextColor.GOLD}>>").setStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand("/rfupb ${currentPage + 1}"))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("Next page")))
            )
        } else {
            Component.literal("${TextColor.DARK_GRAY}>>")
        }

        return Component.literal("\n ")
            .append(previous)
            .append(Component.literal(" ${TextColor.GOLD}Page $currentPage/$totalPages "))
            .append(next)
    }
}
