package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.TrophyFishing
import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.constants.fishing.TrophyTier
import cloud.glitchdev.rfu.constants.fishing.getDonationReward
import cloud.glitchdev.rfu.constants.fishing.getFilletReward
import cloud.glitchdev.rfu.constants.skyblock.Rarity
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.BooleanEntry
import cloud.glitchdev.rfu.data.other.data.StringEntry
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.SetSlotEvents.registerSetSlotEvent
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.gui.window.HudWindow
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import java.util.Locale

@HudElement
object TrophySackDisplay : AbstractTextHudElement("trophySackDisplay") {
    private val SACK_TITLE_REGEX = """^(Bronze|Silver)\s+Trophy\s+(Fishing|Frog)\s+Sack$""".toRegex(RegexOption.IGNORE_CASE)
    private val STORED_REGEX = """Stored:\s*([0-9,]+)""".toRegex()
    private const val SACK_SORT_ORDER_KEY = "trophy_sack_sort_order"
    private const val SACK_COMPACT_KEY = "trophy_sack_compact"

    override val renderOnHud: Boolean = false
    override val renderOnInventory: Boolean = true
    override val isClickableOnInventory: Boolean = true
    override val requirement: Boolean
        get() = TrophyFishing.trophySackDisplay

    override val isElementActive: Boolean
        get() = isTrophySack(HudWindow.currentContainerScreen)

    enum class SackSortOrder(val displayName: String) {
        RARITY("Rarity"),
        MAGMAFISH("Magmafish"),
        QUANTITY("Quantity");

        fun getDisplayName(isFish: Boolean = true): String {
            return if (this == MAGMAFISH && !isFish) "Lotuses" else displayName
        }
    }

    private var sackSortOrder: SackSortOrder
        get() {
            val str = (OtherManager.getField(SACK_SORT_ORDER_KEY) as? StringEntry)?.value
            return runCatching { SackSortOrder.valueOf(str!!) }.getOrDefault(SackSortOrder.RARITY)
        }
        set(value) {
            OtherManager.setField(SACK_SORT_ORDER_KEY, StringEntry(value.name))
            OtherManager.file.save()
        }

    private var isCompact: Boolean
        get() {
            val entry = (OtherManager.getField(SACK_COMPACT_KEY) as? BooleanEntry)?.value
            return entry ?: true // compact by default
        }
        set(value) {
            OtherManager.setField(SACK_COMPACT_KEY, BooleanEntry(value))
            OtherManager.file.save()
        }

    data class TrophySackItem(
        val name: String,
        val rarity: Rarity,
        val color: TextColor,
        val storedCount: Int,
        val unitReward: Int,
        val totalReward: Long
    )

    data class SackData(
        val tier: TrophyTier,
        val isFish: Boolean,
        val items: List<TrophySackItem>,
        val totalItems: Long,
        val totalReward: Long
    )

    private var currentSackData: SackData? = null

    override fun onInitialize() {
        super.onInitialize()

        registerContainerOpenEvent { _, _, _ ->
            if (isTrophySack(HudWindow.currentContainerScreen)) {
                updateState()
            }
        }

        registerSetSlotEvent { _, _, _ ->
            if (isTrophySack(HudWindow.currentContainerScreen)) {
                updateState()
            }
        }
    }

    private fun isTrophySack(screen: AbstractContainerScreen<*>?): Boolean {
        if (screen == null) return false
        val title = screen.title.toUnformattedString().trim()
        return SACK_TITLE_REGEX.matches(title)
    }

    override fun onUpdateState() {
        super.onUpdateState()
        text.clearLines()

        if (isEditing) {
            showPreview()
            return
        }

        val screen = HudWindow.currentContainerScreen
        if (screen == null) {
            currentSackData = null
            return
        }

        val parsed = parseContainer(screen)
        currentSackData = parsed
        if (parsed == null) return

        buildDisplay(parsed)
    }

    private fun parseContainer(screen: AbstractContainerScreen<*>): SackData? {
        val title = screen.title.toUnformattedString().trim()
        val match = SACK_TITLE_REGEX.matchEntire(title) ?: return null

        val tier = runCatching { TrophyTier.valueOf(match.groupValues[1].uppercase()) }.getOrNull() ?: return null
        val isFish = match.groupValues[2].equals("Fishing", ignoreCase = true)

        val containerSlots = screen.menu.slots.filter {
            it.container !is Inventory && it.container != mc.player?.inventory
        }

        val parsedItems = mutableListOf<TrophySackItem>()

        for (slot in containerSlots) {
            val itemStack = slot.item
            if (itemStack.isEmpty) continue

            val loreLines = itemStack[DataComponents.LORE]?.lines?.map { it.toUnformattedString() } ?: continue
            val storedLine = loreLines.firstOrNull { it.contains("Stored:") } ?: continue
            val countMatch = STORED_REGEX.find(storedLine) ?: continue
            val storedCount = countMatch.groupValues[1].replace(",", "").toIntOrNull() ?: continue

            val itemName = itemStack.hoverName.toUnformattedString().trim()

            if (isFish) {
                val fish = TrophyFish.fromName(itemName) ?: findFishByTag(itemStack)
                if (fish != null) {
                    val unitReward = fish.getFilletReward(tier)
                    val totalReward = storedCount.toLong() * unitReward
                    parsedItems.add(
                        TrophySackItem(
                            name = fish.displayName,
                            rarity = fish.rarity,
                            color = fish.rarity.color,
                            storedCount = storedCount,
                            unitReward = unitReward,
                            totalReward = totalReward
                        )
                    )
                }
            } else {
                val frog = TrophyFrog.fromName(itemName) ?: findFrogByTag(itemStack)
                if (frog != null) {
                    val unitReward = frog.getDonationReward(tier)
                    val totalReward = storedCount.toLong() * unitReward
                    parsedItems.add(
                        TrophySackItem(
                            name = frog.displayName,
                            rarity = frog.rarity,
                            color = frog.rarity.color,
                            storedCount = storedCount,
                            unitReward = unitReward,
                            totalReward = totalReward
                        )
                    )
                }
            }
        }

        val totalItems = parsedItems.sumOf { it.storedCount.toLong() }
        val totalReward = parsedItems.sumOf { it.totalReward }

        return SackData(
            tier = tier,
            isFish = isFish,
            items = parsedItems,
            totalItems = totalItems,
            totalReward = totalReward
        )
    }

    private fun findFishByTag(itemStack: ItemStack): TrophyFish? {
        val sbId = getSkyblockId(itemStack) ?: return null
        val baseId = sbId.replace(Regex("_(BRONZE|SILVER|GOLD|DIAMOND)$"), "")
        return TrophyFish.entries.find { it.name == baseId }
            ?: when (baseId) {
                "LAVA_HORSE" -> TrophyFish.LAVAHORSE
                "SOUL_FISH" -> TrophyFish.SOULFISH
                "OBFUSCATED_FISH_1" -> TrophyFish.OBFUSCATED_1
                "OBFUSCATED_FISH_2" -> TrophyFish.OBFUSCATED_2
                "OBFUSCATED_FISH_3" -> TrophyFish.OBFUSCATED_3
                else -> null
            }
    }

    private fun findFrogByTag(itemStack: ItemStack): TrophyFrog? {
        val sbId = getSkyblockId(itemStack) ?: return null
        val baseId = sbId.replace(Regex("_(BRONZE|SILVER|GOLD|DIAMOND)$"), "")
        return TrophyFrog.entries.find { it.name == baseId }
    }

    private fun getSkyblockId(itemStack: ItemStack): String? {
        val customData = itemStack[DataComponents.CUSTOM_DATA] ?: return null
        val tag = customData.copyTag()
        val extraAttributes = tag.getCompound("ExtraAttributes").orElse(null)
        return extraAttributes?.getString("id")?.orElse(null)?.takeIf { it.isNotEmpty() }
            ?: tag.getString("id").orElse(null)?.takeIf { it.isNotEmpty() }
    }

    private fun buildDisplay(data: SackData) {
        val currencyName = if (data.isFish) "Magmafish" else "Lotuses"
        val actionName = if (data.isFish) "Fillet" else "Donation"
        val headerTitle = if (data.isFish) "${TextColor.GOLD}${BOLD}Trophy Fish Fillet" else "${TextColor.LIGHT_GREEN}${BOLD}Trophy Frogs Donation"
        val currencyColor = if (data.isFish) TextColor.GOLD else TextColor.LIGHT_GREEN
        val itemNoun = if (data.isFish) "fish" else "frogs"

        text.addLine(headerTitle)

        val summaryText = "${TextColor.GRAY}Total $actionName: ${TextColor.YELLOW}${formatNumber(data.totalItems)} $currencyColor${formatNumber(data.totalReward)} $currencyName"
        text.addLine(summaryText)

        if (TrophyFishing.trophySackShowBreakdown) {
            val filtered = data.items.filter { if (TrophyFishing.trophySackShowEmpty) true else it.storedCount > 0 }
            val sorted = sortItems(filtered)

            if (sorted.isEmpty() && data.totalItems == 0L) {
                text.addLine("${TextColor.LIGHT_RED}No $itemNoun in sack")
            } else {
                for (item in sorted) {
                    val normalText = "${item.color}${item.name}${TextColor.GRAY}: ${TextColor.YELLOW}${formatNumber(item.storedCount.toLong())} $currencyColor${formatNumber(item.totalReward)} $currencyName"
                    val mathText = "${item.color}${item.name}${TextColor.GRAY}: ${TextColor.YELLOW}${formatNumber(item.storedCount.toLong())} ${TextColor.GRAY}× ${TextColor.DARK_GRAY}${item.unitReward} ${TextColor.GRAY}= $currencyColor${formatNumber(item.totalReward)} $currencyName"

                    val lineComponent = UIText(normalText).constrain {
                        x = 0.pixels()
                        y = SiblingConstraint()
                        width = ScaledTextConstraint(scale)
                        height = TextAspectConstraint()
                    }

                    lineComponent.onMouseEnter {
                        if (isEditing) return@onMouseEnter
                        lineComponent.setText(mathText)
                    }.onMouseLeave {
                        if (isEditing) return@onMouseLeave
                        lineComponent.setText(normalText)
                    }

                    text.addLine(lineComponent)
                }

                createButtonsRow(data.isFish)
            }
        }
    }

    private fun sortItems(items: List<TrophySackItem>): List<TrophySackItem> {
        return when (sackSortOrder) {
            SackSortOrder.RARITY -> items.sortedWith(
                compareByDescending<TrophySackItem> { it.rarity.ordinal }
                    .thenByDescending { it.storedCount }
                    .thenBy { it.name }
            )
            SackSortOrder.MAGMAFISH -> items.sortedWith(
                compareByDescending<TrophySackItem> { it.totalReward }
                    .thenByDescending { it.storedCount }
                    .thenByDescending { it.rarity.ordinal }
                    .thenBy { it.name }
            )
            SackSortOrder.QUANTITY -> items.sortedWith(
                compareByDescending<TrophySackItem> { it.storedCount }
                    .thenByDescending { it.rarity.ordinal }
                    .thenBy { it.name }
            )
        }
    }

    private fun createButtonsRow(isFish: Boolean = true) {
        val buttonsRow = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        val currentOrder = sackSortOrder
        val orderName = currentOrder.getDisplayName(isFish)
        val sortButtonNormal = "${TextColor.DARK_GRAY}[${TextColor.GRAY}Sort: ${TextColor.GOLD}$orderName${TextColor.DARK_GRAY}]"
        val sortButtonHovered = "${TextColor.DARK_GRAY}[${TextColor.GRAY}Sort: ${TextColor.YELLOW}$orderName${TextColor.DARK_GRAY}]"

        val sortButton = UIText(sortButtonNormal).constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = ScaledTextConstraint(scale * 0.9f)
            height = TextAspectConstraint()
        } childOf buttonsRow

        sortButton.onMouseEnter {
            if (isEditing) return@onMouseEnter
            sortButton.setText(sortButtonHovered)
        }.onMouseLeave {
            if (isEditing) return@onMouseLeave
            sortButton.setText(sortButtonNormal)
        }.onMouseClick { event ->
            if (isEditing) return@onMouseClick
            event.stopPropagation()
            sackSortOrder = when (sackSortOrder) {
                SackSortOrder.RARITY -> SackSortOrder.MAGMAFISH
                SackSortOrder.MAGMAFISH -> SackSortOrder.QUANTITY
                SackSortOrder.QUANTITY -> SackSortOrder.RARITY
            }
            updateState()
        }

        val compactState = if (isCompact) "On" else "Off"
        val compactButtonNormal = "${TextColor.DARK_GRAY}[${TextColor.GRAY}Compact: ${TextColor.GOLD}$compactState${TextColor.DARK_GRAY}]"
        val compactButtonHovered = "${TextColor.DARK_GRAY}[${TextColor.GRAY}Compact: ${TextColor.YELLOW}$compactState${TextColor.DARK_GRAY}]"

        val compactButton = UIText(compactButtonNormal).constrain {
            x = SiblingConstraint(4f)
            y = CenterConstraint()
            width = ScaledTextConstraint(scale * 0.9f)
            height = TextAspectConstraint()
        } childOf buttonsRow

        compactButton.onMouseEnter {
            if (isEditing) return@onMouseEnter
            compactButton.setText(compactButtonHovered)
        }.onMouseLeave {
            if (isEditing) return@onMouseLeave
            compactButton.setText(compactButtonNormal)
        }.onMouseClick { event ->
            if (isEditing) return@onMouseClick
            event.stopPropagation()
            isCompact = !isCompact
            updateState()
        }

        text.addLine(buttonsRow)
    }

    private fun showPreview() {
        val magmaColor = TextColor.GOLD
        val gray = TextColor.GRAY
        val yellow = TextColor.YELLOW

        text.addLine("${TextColor.GOLD}${BOLD}Trophy Fish Fillet")
        text.addLine("${gray}Total Fillet: $yellow${formatNumber(1180L)} $magmaColor${formatNumber(153596L)} Magmafish")

        if (TrophyFishing.trophySackShowBreakdown) {
            val previewItems = listOf(
                TrophySackItem(TrophyFish.GOLDEN_FISH.displayName, TrophyFish.GOLDEN_FISH.rarity, TrophyFish.GOLDEN_FISH.rarity.color, 360, 400, 144000L),
                TrophySackItem(TrophyFish.OBFUSCATED_1.displayName, TrophyFish.OBFUSCATED_1.rarity, TrophyFish.OBFUSCATED_1.rarity.color, 217, 16, 3472L),
                TrophySackItem(TrophyFish.BLOBFISH.displayName, TrophyFish.BLOBFISH.rarity, TrophyFish.BLOBFISH.rarity.color, 429, 4, 1716L),
                TrophySackItem(TrophyFish.MANA_RAY.displayName, TrophyFish.MANA_RAY.rarity, TrophyFish.MANA_RAY.rarity.color, 27, 40, 1080L),
                TrophySackItem(TrophyFish.LAVAHORSE.displayName, TrophyFish.LAVAHORSE.rarity, TrophyFish.LAVAHORSE.rarity.color, 70, 12, 840L)
            )

            val sorted = sortItems(previewItems)
            for (item in sorted) {
                val normalText = "${item.color}${item.name}$gray: $yellow${formatNumber(item.storedCount.toLong())} $magmaColor${formatNumber(item.totalReward)} Magmafish"
                val mathText = "${item.color}${item.name}$gray: $yellow${formatNumber(item.storedCount.toLong())} $gray× ${TextColor.DARK_GRAY}${item.unitReward} $gray= $magmaColor${formatNumber(item.totalReward)} Magmafish"

                val lineComponent = UIText(normalText).constrain {
                    x = 0.pixels()
                    y = SiblingConstraint()
                    width = ScaledTextConstraint(scale)
                    height = TextAspectConstraint()
                }

                lineComponent.onMouseEnter {
                    if (isEditing) return@onMouseEnter
                    lineComponent.setText(mathText)
                }.onMouseLeave {
                    if (isEditing) return@onMouseLeave
                    lineComponent.setText(normalText)
                }

                text.addLine(lineComponent)
            }

            createButtonsRow()
        }
    }

    private fun formatNumber(number: Long): String {
        return if (isCompact) {
            formatCompact(number)
        } else {
            String.format(Locale.US, "%,d", number)
        }
    }

    private fun formatCompact(value: Long): String {
        return when {
            value >= 1_000_000_000L -> formatDecimal(value / 1_000_000_000.0) + "B"
            value >= 1_000_000L -> formatDecimal(value / 1_000_000.0) + "M"
            value >= 1_000L -> formatDecimal(value / 1_000.0) + "k"
            else -> value.toString()
        }
    }

    private fun formatDecimal(value: Double): String {
        val s = String.format(Locale.US, "%.1f", value)
        return if (s.endsWith(".0")) s.dropLast(2) else s
    }
}
