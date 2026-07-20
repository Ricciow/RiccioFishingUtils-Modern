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
import net.minecraft.network.chat.Component

@RFUFeature
object TrophyCatchMessageReplacer : Feature {
    override fun onInitialize() {
        registerAllowGameEvent(TrophyCatchEvents.TROPHY_FISH_REGEX, priority = 10) { message, overlay, matches ->
            handleTrophyCatchMessage(message, overlay, matches, isFrog = false)
        }

        registerAllowGameEvent(TrophyCatchEvents.TROPHY_FROG_REGEX, priority = 10) { message, overlay, matches ->
            handleTrophyCatchMessage(message, overlay, matches, isFrog = true)
        }
    }

    private fun handleTrophyCatchMessage(
        message: Component,
        overlay: Boolean,
        matches: MatchResult?,
        isFrog: Boolean
    ): Boolean {
        if (overlay) return true
        if (!TrophyFishing.appendPityToTrophyMessage) return true

        val name = matches?.groupValues?.getOrNull(1) ?: return true
        val tierStr = matches.groupValues.getOrNull(2)?.uppercase() ?: return true
        val tier = TrophyTier.entries.find { it.name == tierStr } ?: return true

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

        val progress = when (tier) {
            TrophyTier.GOLD -> existing?.goldProgress ?: 0
            TrophyTier.DIAMOND -> existing?.diamondProgress ?: 0
            else -> null
        }

        val limit = when (tier) {
            TrophyTier.GOLD -> trophy.goldPity
            TrophyTier.DIAMOND -> trophy.diamondPity
            else -> 0
        }

        if (progress != null) {
            val isPity = progress >= limit
            val colorCode = when (tier) {
                TrophyTier.GOLD -> TextColor.GOLD.code
                TrophyTier.DIAMOND -> TextColor.AQUAMARINE.code
                else -> ""
            }

            val suffix = buildString {
                append(" §7($colorCode$progress§7)")
                if (isPity) {
                    append(" ${TextColor.LIGHT_RED}${TextEffects.BOLD}PITY!")
                }
            }

            val modifiedMessage = message.copy().append(Component.literal(suffix))
            Chat.sendMessage(modifiedMessage)
            return false
        }

        return true
    }
}
