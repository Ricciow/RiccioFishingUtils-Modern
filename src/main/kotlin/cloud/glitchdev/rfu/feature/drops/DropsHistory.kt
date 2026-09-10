package cloud.glitchdev.rfu.feature.drops

import cloud.glitchdev.rfu.constants.fishing.IRareDrop
import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.constants.text.TextEffects.*
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.data.drops.DropHistory
import cloud.glitchdev.rfu.data.drops.DropManager
import cloud.glitchdev.rfu.data.drops.DropRecord
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.arguments.DateArgumentType
import cloud.glitchdev.rfu.utils.command.arguments.StringListArgumentType
import cloud.glitchdev.rfu.utils.dsl.toFormattedDate
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import kotlin.math.ceil
import kotlin.time.Clock
import kotlin.time.Instant
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style

object DropsHistory {
    private const val PAGE_SIZE = 10

    private val dropSuggestions: List<String>
        get() = (
            RareDrops.entries.map { it.displayName.uppercase().replace(" ", "_") } +
            Dyes.entries.map { it.displayName.uppercase().replace(" ", "_") }
        ).distinct()

    @Command
    object DropHistoryCommand : AbstractCommand("rfudrophistory") {
        override val description: String = "Sends the latest drop for each item you've dropped or detailed information if specified"

        override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
            val addBuilder = lit("add")
                .executes { context ->
                    context.source.sendFeedback(
                        TextUtils.rfuLiteral("Usage: /rfudrophistory add <dropName> [date] [mf] [count] [mob]", YELLOW)
                    )
                    1
                }
                .then(
                    arg("dropName", StringListArgumentType(dropSuggestions, greedy = false, exclusive = false))
                        .executes { context ->
                            val dropName = StringArgumentType.getString(context, "dropName")
                            handleAddDrop(context.source, dropName, Clock.System.now(), null, null, null)
                            1
                        }
                        .then(
                            arg("date", DateArgumentType.date())
                                .executes { context ->
                                    val dropName = StringArgumentType.getString(context, "dropName")
                                    val date = DateArgumentType.getDate(context, "date")
                                    handleAddDrop(context.source, dropName, date, null, null, null)
                                    1
                                }
                                .then(
                                    arg("mf", IntegerArgumentType.integer(0))
                                        .executes { context ->
                                            val dropName = StringArgumentType.getString(context, "dropName")
                                            val date = DateArgumentType.getDate(context, "date")
                                            val mf = IntegerArgumentType.getInteger(context, "mf")
                                            handleAddDrop(context.source, dropName, date, mf, null, null)
                                            1
                                        }
                                        .then(
                                            arg("count", IntegerArgumentType.integer(0))
                                                .executes { context ->
                                                    val dropName = StringArgumentType.getString(context, "dropName")
                                                    val date = DateArgumentType.getDate(context, "date")
                                                    val mf = IntegerArgumentType.getInteger(context, "mf")
                                                    val count = IntegerArgumentType.getInteger(context, "count")
                                                    handleAddDrop(context.source, dropName, date, mf, count, null)
                                                    1
                                                }
                                                .then(
                                                    arg("mob", StringArgumentType.greedyString())
                                                        .executes { context ->
                                                            val dropName = StringArgumentType.getString(context, "dropName")
                                                            val date = DateArgumentType.getDate(context, "date")
                                                            val mf = IntegerArgumentType.getInteger(context, "mf")
                                                            val count = IntegerArgumentType.getInteger(context, "count")
                                                            val mob = StringArgumentType.getString(context, "mob")
                                                            handleAddDrop(context.source, dropName, date, mf, count, mob)
                                                            1
                                                        }
                                                )
                                        )
                                )
                        )
                )

            val removeBuilder = lit("remove")
                .executes { context ->
                    context.source.sendFeedback(
                        TextUtils.rfuLiteral("Usage: /rfudrophistory remove <dropName> <index>", YELLOW)
                    )
                    1
                }
                .then(
                    arg("dropName", StringListArgumentType(dropSuggestions, greedy = false, exclusive = false))
                        .executes { context ->
                            val dropName = StringArgumentType.getString(context, "dropName")
                            context.source.sendFeedback(
                                TextUtils.rfuLiteral("Usage: /rfudrophistory remove $dropName <index>", YELLOW)
                            )
                            1
                        }
                        .then(
                            arg("index", IntegerArgumentType.integer(1))
                                .executes { context ->
                                    val dropName = StringArgumentType.getString(context, "dropName")
                                    val index = IntegerArgumentType.getInteger(context, "index")
                                    handleRemoveDrop(context.source, dropName, index)
                                    1
                                }
                        )
                )

            builder
                .executes { context ->
                    context.source.sendFeedback(allDropsMessage(1))
                    1
                }
                .then(addBuilder)
                .then(removeBuilder)
                .then(
                    arg("query", StringListArgumentType(dropSuggestions, greedy = false, exclusive = false))
                        .executes { context ->
                            val query = StringArgumentType.getString(context, "query")
                            val pageAsInt = query.toIntOrNull()
                            val message = if (pageAsInt != null) {
                                allDropsMessage(pageAsInt)
                            } else {
                                singleDropMessage(query, 1)
                            }
                            context.source.sendFeedback(message)
                            1
                        }
                        .then(
                            arg("page", IntegerArgumentType.integer(1))
                                .executes { context ->
                                    val query = StringArgumentType.getString(context, "query")
                                    val page = IntegerArgumentType.getInteger(context, "page")
                                    context.source.sendFeedback(singleDropMessage(query, page))
                                    1
                                }
                        )
                )
        }
    }

    fun findDrop(query: String): IRareDrop? {
        val target = query.trim().uppercase().replace(" ", "_")
        return RareDrops.entries.find { it.displayName.uppercase().replace(" ", "_") == target || it.name == target }
            ?: Dyes.entries.find { it.displayName.uppercase().replace(" ", "_") == target || it.name == target }
    }

    private fun handleAddDrop(
        source: FabricClientCommandSource,
        dropName: String,
        parsedDate: Instant = Clock.System.now(),
        magicFind: Int?,
        customCount: Int?,
        mobName: String? = null
    ) {
        val drop = findDrop(dropName)
        if (drop == null) {
            source.sendFeedback(TextUtils.rfuLiteral("Drop '$dropName' does not exist!", LIGHT_RED))
            return
        }

        val entry: DropHistory.IDropEntry = when (drop) {
            is RareDrops -> DropManager.dropHistory.getOrAdd(drop)
            is Dyes -> DropManager.dropHistory.getOrAdd(drop)
            else -> {
                source.sendFeedback(TextUtils.rfuLiteral("Failed to add drop for '$dropName'.", LIGHT_RED))
                return
            }
        }

        val count = if (drop.relatedScs.isEmpty()) null else drop.relatedScs.sumOf { sc ->
            CatchTracker.catchHistory.getOrAdd(sc).total
        }

        val resolvedMob = mobName ?: if (drop.relatedScs.size == 1) drop.relatedScs.first().scName else null

        entry.addDrop(count, magicFind = magicFind, date = parsedDate, mobName = resolvedMob, sinceCount = customCount)
        DropManager.dropsFile.save()

        val extraInfo = buildString {
            if (resolvedMob != null) append(" from $resolvedMob")
            if (magicFind != null) append(" ($magicFind% \uE01A)")
            if (customCount != null) append(" [count: $customCount]")
        }

        source.sendFeedback(
            TextUtils.rfuLiteral("Successfully added drop for ${drop.displayName} on ${parsedDate.toFormattedDate()}$extraInfo.", LIGHT_GREEN)
        )
    }

    private fun handleRemoveDrop(source: FabricClientCommandSource, dropName: String, index: Int) {
        val drop = findDrop(dropName)
        if (drop == null) {
            source.sendFeedback(TextUtils.rfuLiteral("Drop '$dropName' does not exist!", LIGHT_RED))
            return
        }

        val entry: DropHistory.IDropEntry = when (drop) {
            is RareDrops -> DropManager.dropHistory.getOrAdd(drop)
            is Dyes -> DropManager.dropHistory.getOrAdd(drop)
            else -> {
                source.sendFeedback(TextUtils.rfuLiteral("Failed to find drop records for '$dropName'.", LIGHT_RED))
                return
            }
        }

        if (entry.history.isEmpty()) {
            source.sendFeedback(TextUtils.rfuLiteral("No drop records found for ${drop.displayName}.", LIGHT_RED))
            return
        }

        val removed = entry.removeDrop(index)
        if (removed == null) {
            source.sendFeedback(
                TextUtils.rfuLiteral("Invalid index $index. Must be between 1 and ${entry.history.size}.", LIGHT_RED)
            )
            return
        }

        DropManager.dropsFile.save()
        val mobPart = removed.mobName?.let { " from $it" } ?: ""
        source.sendFeedback(
            TextUtils.rfuLiteral("Successfully removed drop #$index (${removed.date.toFormattedDate()}$mobPart) for ${drop.displayName}.", LIGHT_GREEN)
        )
    }

    private data class DropOverviewItem(
        val drop: IRareDrop,
        val history: List<DropRecord>,
        val lastDrop: DropRecord
    )

    private fun allDropsMessage(page: Int): Component {
        val text = TextUtils.rfuLiteral("Drop History:", GOLD)

        val allItems = mutableListOf<DropOverviewItem>()
        DropManager.dropHistory.drops.forEach { entry ->
            val last = entry.history.lastOrNull()
            if (last != null) {
                allItems.add(DropOverviewItem(entry.type, entry.history, last))
            }
        }
        DropManager.dropHistory.dyeDrops.forEach { entry ->
            val last = entry.history.lastOrNull()
            if (last != null) {
                allItems.add(DropOverviewItem(entry.type, entry.history, last))
            }
        }

        if (allItems.isEmpty()) {
            return text.append(Component.literal("\n $LIGHT_RED${BOLD}No drops :("))
        }

        allItems.sortByDescending { it.lastDrop.date }

        val totalPages = maxOf(1, ceil(allItems.size.toDouble() / PAGE_SIZE).toInt())
        val currentPage = page.coerceIn(1, totalPages)
        val startIndex = (currentPage - 1) * PAGE_SIZE
        val endIndex = minOf(startIndex + PAGE_SIZE, allItems.size)
        val pageItems = allItems.subList(startIndex, endIndex)

        pageItems.forEach { item ->
            try {
                val itemName = item.drop.displayName
                val totalCount = item.history.size
                val lastDrop = item.lastDrop
                val sincePart = lastDrop.sinceCount?.let { " ($it)" } ?: ""
                val mfPart = lastDrop.magicFind?.let { " $AQUAMARINE(${it}% \uE01A)" } ?: ""
                val mobPart = lastDrop.mobName?.let { " $DARK_GRAY($GRAY$it$DARK_GRAY)" } ?: ""
                val name = item.drop.displayName.uppercase().replace(" ", "_")
                val itemCmd = "/rfudrophistory $name"

                val breakdownStr = if (item.drop.relatedScs.size > 1) {
                    val breakdown = item.drop.relatedScs.map { sc ->
                        sc.scName to item.history.count { it.mobName.equals(sc.scName, ignoreCase = true) }
                    }
                    val relevant = if (item.drop.relatedScs.size <= 4) breakdown else breakdown.filter { it.second > 0 }
                    if (relevant.isNotEmpty()) " $DARK_GRAY(${relevant.joinToString(", ") { "$GRAY${it.first}: $WHITE${it.second}" }}$DARK_GRAY)" else ""
                } else ""

                val line = Component.literal("\n $YELLOW$BOLD- $WHITE$itemName: $YELLOW$totalCount$breakdownStr ${YELLOW}- Last: $WHITE${lastDrop.date.toFormattedDate()}$WHITE$sincePart$mfPart$mobPart")
                    .setStyle(
                        Style.EMPTY
                            .withClickEvent(ClickEvent.RunCommand(itemCmd))
                            .withHoverEvent(HoverEvent.ShowText(Component.literal("Click to view $itemName history")))
                    )
                text.append(line)
            } catch (e: Exception) {
                RFULogger.error("Error on rfudrophistory:", e)
            }
        }

        text.append(buildFooter(currentPage, totalPages, "/rfudrophistory"))
        return text
    }

    private fun singleDropMessage(query: String, page: Int): Component {
        val drop = findDrop(query)
            ?: return TextUtils.rfuLiteral("Drop '$query' does not exist!", LIGHT_RED)

        val entry: DropHistory.IDropEntry = when (drop) {
            is RareDrops -> DropManager.dropHistory.getOrAdd(drop)
            is Dyes -> DropManager.dropHistory.getOrAdd(drop)
            else -> return TextUtils.rfuLiteral("Drop '$query' does not exist!", LIGHT_RED)
        }

        return singleDropHistoryMessage(drop, entry.history, page)
    }

    private fun singleDropHistoryMessage(drop: IRareDrop, history: List<DropRecord>, page: Int): Component {
        val text = TextUtils.rfuLiteral("${drop.displayName}:", GOLD)

        if (history.isEmpty()) {
            return text.append(Component.literal("\n $LIGHT_RED${BOLD}No drops :("))
        }

        val breakdownStr = if (drop.relatedScs.size > 1) {
            val breakdown = drop.relatedScs.map { sc ->
                sc.scName to history.count { it.mobName.equals(sc.scName, ignoreCase = true) }
            }
            val relevant = if (drop.relatedScs.size <= 4) breakdown else breakdown.filter { it.second > 0 }
            if (relevant.isNotEmpty()) " $DARK_GRAY(${relevant.joinToString(", ") { "$GRAY${it.first}: $WHITE${it.second}" }}$DARK_GRAY)" else ""
        } else ""

        text.append(Component.literal("\n $YELLOW${BOLD}Total: $WHITE${history.size}$breakdownStr"))

        val reversedHistory = history.asReversed()
        val totalPages = maxOf(1, ceil(reversedHistory.size.toDouble() / PAGE_SIZE).toInt())
        val currentPage = page.coerceIn(1, totalPages)
        val startIndex = (currentPage - 1) * PAGE_SIZE
        val endIndex = minOf(startIndex + PAGE_SIZE, reversedHistory.size)
        val pageItems = reversedHistory.subList(startIndex, endIndex)

        pageItems.forEachIndexed { i, record ->
            val displayIndex = startIndex + i + 1
            val sincePart = record.sinceCount?.let { "$WHITE$it " } ?: ""
            val mfPart = record.magicFind?.let { "$AQUAMARINE($it% \uE01A) " } ?: ""
            val mobPart = record.mobName?.let { "$GRAY($it) " } ?: ""
            text.append(Component.literal("\n $GRAY$displayIndex - $YELLOW${record.date.toFormattedDate()}$YELLOW: $sincePart$mfPart$mobPart"))
        }

        val name = drop.displayName.uppercase().replace(" ", "_")
        val baseCommand = "/rfudrophistory $name"
        text.append(buildFooter(currentPage, totalPages, baseCommand))
        return text
    }

    private fun buildFooter(currentPage: Int, totalPages: Int, baseCommand: String): Component {
        val footer = Component.literal("\n ")

        val prevBtn = if (currentPage > 1) {
            Component.literal("$GOLD<<").setStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand("$baseCommand ${currentPage - 1}"))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("Previous page")))
            )
        } else {
            Component.literal("$DARK_GRAY<<")
        }

        val pageText = Component.literal(" $GOLD Page $currentPage/$totalPages ")

        val nextBtn = if (currentPage < totalPages) {
            Component.literal("$GOLD>>").setStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand("$baseCommand ${currentPage + 1}"))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("Next page")))
            )
        } else {
            Component.literal("$DARK_GRAY>>")
        }

        return footer.append(prevBtn).append(pageText).append(nextBtn)
    }
}