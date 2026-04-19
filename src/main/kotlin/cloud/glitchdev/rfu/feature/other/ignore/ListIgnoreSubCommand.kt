package cloud.glitchdev.rfu.feature.other.ignore

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlin.math.ceil

object ListIgnoreSubCommand : AbstractCommand("list") {
    override val description: String = "List ignored users"

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            arg("page", IntegerArgumentType.integer(1)).executes { context ->
                val page = IntegerArgumentType.getInteger(context, "page")
                listIgnored(context.source, page)
                1
            }
        ).executes { context ->
            listIgnored(context.source, 1)
            1
        }
    }

    private fun listIgnored(source: FabricClientCommandSource, page: Int) {
        val entry = IgnoreUtils.getIgnoredEntry()
        val all = entry.getAll()
        if (all.isEmpty()) {
            source.sendFeedback(TextUtils.rfuLiteral("The ignore list is empty.", TextColor.YELLOW))
            return
        }

        val pageSize = 10
        val totalPages = ceil(all.size.toDouble() / pageSize).toInt()
        val currentPage = page.coerceIn(1, totalPages)
        
        val startIndex = (currentPage - 1) * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(all.size)
        val pageItems = all.subList(startIndex, endIndex)

        val header = TextUtils.rfuLiteral("${TextColor.YELLOW}--- Ignored Users (Page $currentPage/$totalPages) ---")
        source.sendFeedback(header)
        
        pageItems.forEach { user ->
            source.sendFeedback(TextUtils.rfuLiteral("${TextColor.GRAY}- ${TextColor.GOLD}$user"))
        }
    }
}
