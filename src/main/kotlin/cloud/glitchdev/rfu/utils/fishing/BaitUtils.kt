package cloud.glitchdev.rfu.utils.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import java.util.Locale

object BaitUtils {
    private val BAIT_COUNT_REGEX = """Bait Remaining: ([\d,]+)""".toExactRegex()

    fun getBaitRemainingCount(item: ItemStack): Int? {
        if (item.isEmpty) return null
        val loreLines = item[DataComponents.LORE]?.lines ?: return null
        for (line in loreLines) {
            val unformatted = line.toUnformattedString()
            if (!unformatted.contains("Bait Remaining:")) continue
            val match = BAIT_COUNT_REGEX.find(unformatted) ?: continue
            val countStr = match.groupValues.getOrNull(1)?.replace(",", "") ?: continue
            return countStr.toIntOrNull()
        }
        return null
    }

    fun formatBaitCount(count: Int): String {
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

    @JvmStatic
    fun getCustomBaitCountText(item: ItemStack): String? {
        if (!GeneralFishing.showActualBaitCount) return null
        val count = getBaitRemainingCount(item) ?: return null
        return formatBaitCount(count)
    }
}
