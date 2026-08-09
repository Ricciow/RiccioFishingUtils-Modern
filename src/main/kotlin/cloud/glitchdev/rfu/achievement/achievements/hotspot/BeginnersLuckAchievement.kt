package cloud.glitchdev.rfu.achievement.achievements.hotspot

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.utils.dsl.isFishingRod
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack

@Achievement
object BeginnersLuckAchievement : BaseAchievement() {
    override val id: String = "beginners_luck"
    override val name: String = "Begginer's Luck"
    override val description: String = "Fish up a Wiki Tiki using an unenchanted, unreforged rod with no rod parts and no bait."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.HOT_SPOT

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { sc, _, _, _, bait ->
            if (sc.scName == "Wiki Tiki" && bait == null) {
                val player = mc.player ?: return@registerSeaCreatureCatchEvent
                val mainHand = player.mainHandItem
                val item = if (mainHand.isFishingRod()) mainHand else player.offhandItem.takeIf { it.isFishingRod() } ?: return@registerSeaCreatureCatchEvent

                if (isUnenchanted(item) && isUnreforged(item) && hasNoRodParts(item)) {
                    complete()
                }
            }
        })
    }

    private fun isUnenchanted(item: ItemStack): Boolean {
        val enchantments = item[DataComponents.ENCHANTMENTS]
        if (enchantments != null && !enchantments.isEmpty) return false

        val customData = item[DataComponents.CUSTOM_DATA]
        val tag = customData?.copyTag()
        val extraAttributes = tag?.getCompound("ExtraAttributes")?.orElse(null)
        val sbEnchants = extraAttributes?.getCompound("enchantments")?.orElse(null)
        if (sbEnchants != null && !sbEnchants.isEmpty) return false

        return true
    }

    private fun isUnreforged(item: ItemStack): Boolean {
        val customData = item[DataComponents.CUSTOM_DATA]
        val tag = customData?.copyTag()
        val extraAttributes = tag?.getCompound("ExtraAttributes")?.orElse(null)
        val modifier = extraAttributes?.getString("modifier")?.orElse(null)
        if (!modifier.isNullOrEmpty()) return false

        return true
    }

    private fun hasNoRodParts(item: ItemStack): Boolean {
        val customData = item[DataComponents.CUSTOM_DATA]
        val tag = customData?.copyTag()
        val extraAttributes = tag?.getCompound("ExtraAttributes")?.orElse(null)
        if (extraAttributes != null) {
            val hookCompound = extraAttributes.getCompound("hook").orElse(null)
            val lineCompound = extraAttributes.getCompound("line").orElse(null)
            val sinkerCompound = extraAttributes.getCompound("sinker").orElse(null)

            if (hookCompound != null && !hookCompound.isEmpty) return false
            if (lineCompound != null && !lineCompound.isEmpty) return false
            if (sinkerCompound != null && !sinkerCompound.isEmpty) return false

            val hookStr = extraAttributes.getString("rod_hook").orElse(null)
                ?: extraAttributes.getString("hook").orElse(null)
            val lineStr = extraAttributes.getString("rod_line").orElse(null)
                ?: extraAttributes.getString("line").orElse(null)
            val sinkerStr = extraAttributes.getString("rod_sinker").orElse(null)
                ?: extraAttributes.getString("sinker").orElse(null)

            if (!hookStr.isNullOrEmpty() && !hookStr.equals("NONE", ignoreCase = true)) return false
            if (!lineStr.isNullOrEmpty() && !lineStr.equals("NONE", ignoreCase = true)) return false
            if (!sinkerStr.isNullOrEmpty() && !sinkerStr.equals("NONE", ignoreCase = true)) return false
        }

        val lore = item[DataComponents.LORE]
        if (lore != null) {
            for (lineComponent in lore.lines) {
                val text = lineComponent.toUnformattedString()
                if (text.contains("Hook ") && !text.contains("Hook NONE")) return false
                if (text.contains("Line ") && !text.contains("Line NONE")) return false
                if (text.contains("Sinker ") && !text.contains("Sinker NONE")) return false
            }
        }

        return true
    }
}
