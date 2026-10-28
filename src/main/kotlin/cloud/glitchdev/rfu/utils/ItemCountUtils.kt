package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import java.util.Locale

object ItemCountUtils {
    private val REMAINING_COUNT_REGEX = """(Bait|Arrows) Remaining: ([\d,]+)""".toExactRegex()

    @JvmStatic
    fun getCustomCountText(item: ItemStack): String? {
        val baitCount = if (GeneralFishing.showActualBaitCount) getRemainingCount(item, "Bait") else null
        val count = baitCount
            ?: (if (OtherSettings.showActualArrowCount) getRemainingCount(item, "Arrows") else null)
            ?: return null
        return formatCount(count)
    }

    fun getRemainingCount(item: ItemStack, type: String): Int? {
        if (item.isEmpty) return null
        val loreLines = item[DataComponents.LORE]?.lines ?: return null
        for (line in loreLines) {
            val unformatted = line.toUnformattedString()
            val match = REMAINING_COUNT_REGEX.find(unformatted) ?: continue
            if (match.groupValues[1] != type) continue
            return match.groupValues[2].replace(",", "").toIntOrNull()
        }
        return null
    }

    private fun formatCount(count: Int): String {
        return when {
            count < 1000 -> count.toString()
            count < 10000 -> {
                val formatted = String.format(Locale.US, "%.1fk", count / 1000.0)
                if (formatted.endsWith(".0k")) "${count / 1000}k" else formatted
            }
            count < 1_000_000 -> "${count / 1000}k"
            else -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
        }
    }
}
