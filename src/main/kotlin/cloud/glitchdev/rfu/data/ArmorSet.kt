package cloud.glitchdev.rfu.data

import cloud.glitchdev.rfu.utils.dsl.hasDescriptionText
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

data class ArmorSet(
    val head: ItemStack = ItemStack.EMPTY,
    val chest: ItemStack = ItemStack.EMPTY,
    val legs: ItemStack = ItemStack.EMPTY,
    val feet: ItemStack = ItemStack.EMPTY
) {
    val items: List<ItemStack> by lazy { listOf(head, chest, legs, feet) }

    val isEmpty: Boolean by lazy { items.all { it.isEmpty } }

    val isWearingAnyArmor: Boolean by lazy { items.any { !it.isEmpty } }

    val isWearingFishingArmor: Boolean by lazy {
        items.any { armorPiece ->
            if (armorPiece.isEmpty) return@any false
            val lore = armorPiece[DataComponents.LORE] ?: return@any false
            lore.lines.any { line ->
                val plainText = line.toUnformattedString()
                fishingStatsKeywords.any { keyword ->
                    plainText.contains(keyword, ignoreCase = true)
                }
            }
        }
    }

    val isWearingTrophyHunterArmor: Boolean by lazy {
        items.any { item ->
            !item.isEmpty && item.hasDescriptionText("Tiered Bonus: Peace Treaty (2/2)")
        }
    }

    val hasBobbinTimeArmor: Boolean by lazy { bobbinTimeRate > 0.0 }

    val bobbinTimeRate: Double by lazy {
        var rateSum = 0.0
        for (armorPiece in items) {
            val rate = getBobbinTimeRate(armorPiece)
            if (rate != null) {
                rateSum += rate
            }
        }
        rateSum
    }

    val isWearingBlazeArmorSet: Boolean by lazy {
        val headName = head.customName?.toUnformattedString() ?: ""
        val chestName = chest.customName?.toUnformattedString() ?: ""
        val legsName = legs.customName?.toUnformattedString() ?: ""
        val feetName = feet.customName?.toUnformattedString() ?: ""

        headName.contains("Blaze Helmet") &&
                chestName.contains("Blaze Chestplate") &&
                legsName.contains("Blaze Leggings") &&
                feetName.contains("Blaze Boots")
    }

    val isWearingMagmaLord10StarredSet: Boolean by lazy {
        items.all { item ->
            val customName = item.customName ?: return@all false
            customName.toUnformattedString().contains("Magma Lord") && is10Starred(customName)
        }
    }

    val trophyHunterArmorTier: Int by lazy {
        val slots = listOf(
            head to "Hunter Helmet",
            chest to "Hunter Chestplate",
            legs to "Hunter Leggings",
            feet to "Hunter Boots"
        )
        var minTier = 4
        var hasFullSet = true
        for (pair in slots) {
            val item = pair.first
            val expectedPieceName = pair.second
            val customName = item.customName?.toUnformattedString() ?: run {
                hasFullSet = false
                break
            }
            if (!customName.contains(expectedPieceName)) {
                hasFullSet = false
                break
            }
            val tier = when {
                customName.contains("Diamond Hunter") -> 4
                customName.contains("Gold Hunter") -> 3
                customName.contains("Silver Hunter") -> 2
                customName.contains("Bronze Hunter") -> 1
                else -> 0
            }
            if (tier == 0) {
                hasFullSet = false
                break
            }
            if (tier < minTier) minTier = tier
        }
        if (hasFullSet) minTier else 0
    }

    fun getValidBobbinCount(minLevel: Int): Int {
        return items.count { piece ->
            if (piece.isEmpty) return@count false
            val lore = piece[DataComponents.LORE] ?: return@count false
            lore.lines.any { lineComponent ->
                getBobbinLevelFromString(lineComponent.string) >= minLevel
            }
        }
    }

    operator fun get(slot: EquipmentSlot): ItemStack = when (slot) {
        EquipmentSlot.HEAD -> head
        EquipmentSlot.CHEST -> chest
        EquipmentSlot.LEGS -> legs
        EquipmentSlot.FEET -> feet
        else -> ItemStack.EMPTY
    }

    fun hasChanged(other: ArmorSet): Boolean {
        return !isSameArmorPiece(head, other.head) ||
                !isSameArmorPiece(chest, other.chest) ||
                !isSameArmorPiece(legs, other.legs) ||
                !isSameArmorPiece(feet, other.feet)
    }

    companion object {
        private fun isSameArmorPiece(a: ItemStack, b: ItemStack): Boolean {
            if (ItemStack.matches(a, b)) return true
            if (a.isEmpty || b.isEmpty) return false

            val nameA = a.hoverName.toUnformattedString()
            val nameB = b.hoverName.toUnformattedString()

            if (nameA.contains("✦") || nameA.contains("✿") || nameB.contains("✦") || nameB.contains("✿")) {
                return a.item == b.item &&
                        nameA == nameB &&
                        a[DataComponents.LORE] == b[DataComponents.LORE]
            }

            return false
        }

        private val fishingStatsKeywords = arrayOf(
            "Fishing Speed",
            "Trophy Chance",
            "Treasure Chance",
            "Double Hook Chance",
            "Sea Creature Chance"
        )

        private val bobbinRegex = Regex("""Bobbin'\s*Time\s+(III|IV|V|3|4|5)""", RegexOption.IGNORE_CASE)
        private const val STAR_COLOR = 0xFF55FF
        private const val STAR_STRING = "✪✪✪✪✪"

        fun fromPlayer(player: Player?): ArmorSet {
            if (player == null) return ArmorSet()
            return ArmorSet(
                head = player.getItemBySlot(EquipmentSlot.HEAD).copy(),
                chest = player.getItemBySlot(EquipmentSlot.CHEST).copy(),
                legs = player.getItemBySlot(EquipmentSlot.LEGS).copy(),
                feet = player.getItemBySlot(EquipmentSlot.FEET).copy()
            )
        }

        private fun getBobbinTimeRate(itemStack: ItemStack): Double? {
            if (itemStack.isEmpty) return null
            val lore = itemStack[DataComponents.LORE] ?: return null
            for (line in lore.lines) {
                val plainText = line.toUnformattedString()
                if (plainText.contains("Bobbin' Time V") || plainText.contains("Bobbin' Time 5")) return 0.01
                if (plainText.contains("Bobbin' Time IV") || plainText.contains("Bobbin' Time 4")) return 0.008
                if (plainText.contains("Bobbin' Time III") || plainText.contains("Bobbin' Time 3")) return 0.006
            }
            return null
        }

        private fun getBobbinLevelFromString(text: String): Int {
            val match = bobbinRegex.find(text) ?: return 0
            return when (val numeral = match.groupValues[1].uppercase()) {
                "III" -> 3
                "IV" -> 4
                "V" -> 5
                else -> numeral.toIntOrNull() ?: 0
            }
        }

        private fun is10Starred(component: Component): Boolean {
            return component.siblings.any { part ->
                val contents = part.contents
                contents is PlainTextContents.LiteralContents &&
                        contents.text.trim() == STAR_STRING &&
                        part.style.color?.value == STAR_COLOR
            }
        }
    }
}
