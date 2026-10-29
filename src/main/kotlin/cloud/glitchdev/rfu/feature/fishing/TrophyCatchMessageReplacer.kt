package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.TrophyFishing
import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.constants.fishing.TrophyTier
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.fishing.TrophyDataManager
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAllowGameEvent
import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents
import cloud.glitchdev.rfu.constants.fishing.Trophy
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.dsl.formatTemplate
import net.minecraft.network.chat.Component

@RFUFeature
object TrophyCatchMessageReplacer : Feature {
    override fun onInitialize() {
        registerAllowGameEvent(TrophyCatchEvents.TROPHY_FISH_REGEX, priority = 10) { _, overlay, matches ->
            handleTrophyCatchMessage(overlay, matches, isFrog = false)
        }

        registerAllowGameEvent(TrophyCatchEvents.TROPHY_FROG_REGEX, priority = 10) { _, overlay, matches ->
            handleTrophyCatchMessage(overlay, matches, isFrog = true)
        }
    }

    fun previewFish() = preview(TrophyFish.entries.random())

    fun previewFrog() = preview(TrophyFrog.entries.random())

    private fun preview(trophy: Trophy) {
        val tier = listOf(TrophyTier.GOLD, TrophyTier.DIAMOND).random()
        Chat.sendMessage(Component.literal(formatMessage(trophy, tier, count = 21, total = 123, progress = 42, amount = 1)))
    }

    private fun handleTrophyCatchMessage(
        overlay: Boolean,
        matches: MatchResult?,
        isFrog: Boolean
    ): Boolean {
        if (overlay) return true
        if (!TrophyFishing.customTrophyMessageEnabled) return true

        val name = matches?.groupValues?.getOrNull(1) ?: return true
        val tierStr = matches.groupValues.getOrNull(2)?.uppercase() ?: return true
        val tier = TrophyTier.entries.find { it.name == tierStr } ?: return true
        val amount = matches.groupValues.getOrNull(3)?.toIntOrNull() ?: 1

        val trophy: Trophy = if (isFrog) {
            TrophyFrog.fromName(name) ?: return true
        } else {
            TrophyFish.fromName(name) ?: return true
        }

        val existing = if (isFrog) {
            TrophyDataManager.data.pity.frogPity[trophy.name]
        } else {
            TrophyDataManager.data.pity.fishPity[trophy.name]
        }

        val counts = if (isFrog) {
            TrophyDataManager.data.count.frogCount[trophy.name]
        } else {
            TrophyDataManager.data.count.fishCount[trophy.name]
        }
        val count = (when (tier) {
            TrophyTier.BRONZE -> counts?.bronze
            TrophyTier.SILVER -> counts?.silver
            TrophyTier.GOLD -> counts?.gold
            TrophyTier.DIAMOND -> counts?.diamond
        } ?: 0) + amount
        val total = (counts?.total ?: 0) + amount
        val progress = when (tier) {
            TrophyTier.GOLD -> existing?.goldProgress ?: 0
            TrophyTier.DIAMOND -> existing?.diamondProgress ?: 0
            else -> null
        }
        Chat.sendMessage(Component.literal(formatMessage(trophy, tier, count, total, progress, amount)))
        return false
    }

    private fun formatMessage(trophy: Trophy, tier: TrophyTier, count: Int, total: Int, progress: Int?, amount: Int): String {
        val tierColor = when (tier) {
            TrophyTier.BRONZE -> TextColor.DARK_GRAY
            TrophyTier.SILVER -> TextColor.GRAY
            TrophyTier.GOLD -> TextColor.GOLD
            TrophyTier.DIAMOND -> TextColor.AQUAMARINE
        }
        val limit = when (tier) {
            TrophyTier.GOLD -> trophy.goldPity
            TrophyTier.DIAMOND -> trophy.diamondPity
            else -> 0
        }

        val pity = if (progress != null) {
            buildString {
                append(TrophyFishing.customTrophyPityCount.formatTemplate("pity" to "$tierColor$progress"))
                if (progress >= limit) append(" ${TextColor.LIGHT_RED}${TextEffects.BOLD}PITY!")
            }
        } else ""
        val suffix = when {
            count % 10 == 1 -> "st"
            count % 10 == 2 -> "nd"
            count % 10 == 3 -> "rd"
            else -> "th"
        }
        val template = if (trophy is TrophyFrog) TrophyFishing.customTrophyFrogMessage else TrophyFishing.customTrophyFishMessage
        return template.formatTemplate(
            "name" to trophy.displayName,
            "color" to trophy.rarity.color.code,
            "tier_color" to tierColor,
            "tier" to tier.name,
            "count" to count,
            "total" to total,
            "ordinal" to suffix,
            "amount" to amount,
            "multi" to if (amount > 1) " x$amount" else "",
            "pity" to pity
        ).trimEnd()
    }
}
