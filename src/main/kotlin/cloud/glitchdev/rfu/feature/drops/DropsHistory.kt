package cloud.glitchdev.rfu.feature.drops

import cloud.glitchdev.rfu.constants.fishing.IRareDrop
import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.constants.text.TextEffects.*
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.ChatComponent
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style

object DropsHistory {
    private const val PAGE_SIZE = 10
    private val MAGIC_FIND_UPDATE = Instant.parse("2026-11-17T10:00:00Z")
    private val OVERVIEW_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yy")

    private fun dropNameComponent(drop: IRareDrop): MutableComponent {
        val style = if (drop is Dyes) {
            Style.EMPTY.withColor(drop.hex.toInt(16))
        } else {
            Style.EMPTY.withColor(ChatFormatting.getByCode(drop.rarity.color.code.last()))
        }
        return Component.literal(drop.displayName).setStyle(style)
    }

    private fun preMagicFindUpdateWarning(record: DropRecord): Component {
        if (record.date >= MAGIC_FIND_UPDATE) return Component.empty()

        return Component.literal(" $GOLD[!]$RESET").setStyle(
            Style.EMPTY.withHoverEvent(
                HoverEvent.ShowText(
                    Component.literal(
                        "$YELLOW${BOLD}Old Magic Find\n" +
                            "${GRAY}This drop occurred before the Magic Find update."
                    )
                )
            )
        )
    }

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

        val isLatest = entry.history.lastOrNull()?.let { parsedDate >= it.date } ?: true
        val count = customCount ?: if (drop.relatedScs.isNotEmpty() && isLatest) entry.currentCount else null

        val resolvedMob = mobName ?: if (drop.relatedScs.size == 1) drop.relatedScs.first().scName else null

        entry.addDrop(count, magicFind = magicFind, date = parsedDate, mobName = resolvedMob, resetCounter = isLatest)
        DropManager.dropsFile.save()

        val extraInfo = buildString {
            if (resolvedMob != null) append(" from $resolvedMob")
            if (magicFind != null) append(" ($magicFind\uE01A)")
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

        val rows = allItems.mapNotNull { item ->
            try {
                val totalCount = item.history.size
                val lastDrop = item.lastDrop
                val name = item.drop.displayName.uppercase().replace(" ", "_")
                val itemCmd = "/rfudrophistory $name"

                val breakdownStr = if (item.drop.relatedScs.size > 1) {
                    val breakdown = item.drop.relatedScs.map { sc ->
                        sc.scName to item.history.count { it.mobName.equals(sc.scName, ignoreCase = true) }
                    }
                    val relevant = if (item.drop.relatedScs.size <= 4) breakdown else breakdown.filter { it.second > 0 }
                    if (relevant.isNotEmpty()) " $DARK_GRAY(${relevant.joinToString(", ") { "$GRAY${it.first}: $WHITE${it.second}" }}$DARK_GRAY)" else ""
                } else ""

                val details = dropNameComponent(item.drop)
                    .append(Component.literal("\n${YELLOW}Total: $WHITE$totalCount$breakdownStr"))
                    .append(Component.literal("\n${YELLOW}Last drop: $WHITE${lastDrop.date.toFormattedDate()}"))
                lastDrop.sinceCount?.let { details.append(Component.literal("\n${YELLOW}Catches Since: $WHITE$it")) }
                lastDrop.magicFind?.let { details.append(Component.literal("\n${AQUAMARINE}Magic Find: $it\uE01A")) }
                lastDrop.mobName?.let { details.append(Component.literal("\n${YELLOW}Creature: ${GRAY}$it")) }
                details.append(Component.literal("\n${DARK_GRAY}Click to view history"))

                val style = Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand(itemCmd))
                    .withHoverEvent(HoverEvent.ShowText(details))
                listOf(
                    Component.literal("$YELLOW$BOLD-"),
                    dropNameComponent(item.drop).append(Component.literal("$GRAY:")),
                    Component.literal("$YELLOW$totalCount"),
                    Component.literal("$WHITE${OVERVIEW_DATE_FORMAT.format(lastDrop.date.toJavaInstant().atZone(ZoneId.systemDefault()))}"),
                    Component.literal(lastDrop.magicFind?.let { "$AQUAMARINE($it\uE01A)" } ?: ""),
                    Component.literal(lastDrop.mobName?.let { "$GRAY($it)" } ?: ""),
                    preMagicFindUpdateWarning(lastDrop)
                ) to style
            } catch (e: Exception) {
                RFULogger.error("Error on rfudrophistory:", e)
                null
            }
        }
        val mc = Minecraft.getInstance()
        val chatWidth = (ChatComponent.getWidth(mc.options.chatWidth().get()) / mc.options.chatScale().get()).toInt() - 4
        // Fit all entries before pagination so column positions and visibility stay the same on every page.
        val lines = fitOverviewRows(rows.map { it.first }, mc.font::width, chatWidth)
        val totalPages = maxOf(1, ceil(lines.size.toDouble() / PAGE_SIZE).toInt())
        val currentPage = page.coerceIn(1, totalPages)
        val startIndex = (currentPage - 1) * PAGE_SIZE
        lines.drop(startIndex).take(PAGE_SIZE).forEachIndexed { index, line ->
            text.append(line.copy().setStyle(rows[startIndex + index].second))
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
        val text = TextUtils.rfuLiteral("")
            .append(dropNameComponent(drop))
            .append(Component.literal("$GRAY:"))

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

        val rows = pageItems.mapIndexed { i, record ->
            listOf(
                Component.literal("$GRAY${startIndex + i + 1}"),
                Component.literal("$GRAY-"),
                Component.literal("$YELLOW${record.date.toFormattedDate()}$GRAY:"),
                Component.literal(record.sinceCount?.let { "$WHITE$it" } ?: ""),
                Component.literal(record.magicFind?.let { "$AQUAMARINE($it\uE01A)" } ?: ""),
                Component.literal(record.mobName?.let { "$GRAY($it)" } ?: ""),
                preMagicFindUpdateWarning(record)
            )
        }
        alignHistoryRows(rows, Minecraft.getInstance().font::width).forEach { text.append(it) }

        val name = drop.displayName.uppercase().replace(" ", "_")
        val baseCommand = "/rfudrophistory $name"
        text.append(buildFooter(currentPage, totalPages, baseCommand))
        return text
    }

    private fun fitOverviewRows(
        rows: List<List<Component>>,
        width: (Component) -> Int,
        maxWidth: Int
    ): List<Component> {
        if (rows.isEmpty()) return emptyList()

        val cells = rows.map { it.toMutableList() }
        fun layout() = alignHistoryRows(cells, width)
        fun lineWidth(line: Component) = line.siblings.sumOf(width)
        var lines = layout()
        // Preserve the short date and Magic Find before the optional creature column.
        for (column in listOf(5, 4, 3)) {
            if (lines.all { lineWidth(it) <= maxWidth }) return lines
            cells.forEach { it[column] = Component.empty() }
            lines = layout()
        }

        val names = rows.map { it[1].plainCopy().string }
        for (length in names.maxOf { it.length } downTo 0) {
            if (lines.all { lineWidth(it) <= maxWidth }) return lines
            cells.forEachIndexed { index, row ->
                if (names[index].length > length) {
                    row[1] = Component.literal("${names[index].take(length)}...")
                        .setStyle(rows[index][1].style)
                        .append(Component.literal("$GRAY:"))
                }
            }
            lines = layout()
        }
        return lines
    }

    private fun alignHistoryRows(rows: List<List<Component>>, width: (Component) -> Int): List<Component> {
        if (rows.isEmpty()) return emptyList()

        val spaceWidth = width(Component.literal(" ")).coerceAtLeast(1)
        val boldSpaceWidth = width(Component.literal("$BOLD $RESET")).coerceAtLeast(1)
        val cellWidths = rows.map { row -> row.map(width) }
        val lineWidths = IntArray(rows.size)
        val lines = rows.map { Component.literal("\n") }

        rows.first().indices.forEach { column ->
            val populatedRows = rows.indices.filter { cellWidths[it][column] > 0 }
            if (populatedRows.isEmpty()) return@forEach

            val minEdge = populatedRows.maxOf { row -> lineWidths[row] } + spaceWidth
            val edge = (minEdge..minEdge + spaceWidth * boldSpaceWidth).firstOrNull { candidate ->
                populatedRows.all { row ->
                    val gap = candidate - lineWidths[row]
                    val padding = columnPadding(gap, spaceWidth, boldSpaceWidth)
                    width(Component.literal(padding)) == gap
                }
            } ?: minEdge

            populatedRows.forEach { row ->
                val gap = edge - lineWidths[row]
                val padding = Component.literal(columnPadding(gap, spaceWidth, boldSpaceWidth))
                lines[row].append(padding).append(rows[row][column])
                lineWidths[row] += width(padding) + cellWidths[row][column]
            }
        }
        return lines
    }

    private fun columnPadding(width: Int, spaceWidth: Int, boldSpaceWidth: Int): String {
        if (width <= 0) return ""

        for (boldSpaces in 0..spaceWidth) {
            val remaining = width - boldSpaces * boldSpaceWidth
            if (remaining >= 0 && remaining % spaceWidth == 0) {
                return " ".repeat(remaining / spaceWidth) +
                    if (boldSpaces > 0) "$BOLD${" ".repeat(boldSpaces)}$RESET" else ""
            }
        }
        return " ".repeat((width + spaceWidth / 2) / spaceWidth)
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
