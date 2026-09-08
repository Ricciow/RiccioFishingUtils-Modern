package cloud.glitchdev.rfu.constants.fishing

import cloud.glitchdev.rfu.constants.text.TextColor

object TrophySackRewards {
    private val FISH_FILLET_REWARDS = mapOf(
        TrophyFish.BLOBFISH to intArrayOf(4, 8, 12, 16),
        TrophyFish.FLYFISH to intArrayOf(32, 48, 64, 96),
        TrophyFish.GOLDEN_FISH to intArrayOf(400, 700, 1000, 1300),
        TrophyFish.GUSHER to intArrayOf(32, 48, 64, 96),
        TrophyFish.KARATE_FISH to intArrayOf(40, 60, 80, 120),
        TrophyFish.LAVAHORSE to intArrayOf(12, 16, 20, 24),
        TrophyFish.MANA_RAY to intArrayOf(40, 60, 80, 120),
        TrophyFish.MOLDFIN to intArrayOf(32, 48, 64, 96),
        TrophyFish.SKELETON_FISH to intArrayOf(32, 48, 64, 96),
        TrophyFish.SLUGFISH to intArrayOf(40, 60, 80, 120),
        TrophyFish.SOULFISH to intArrayOf(32, 48, 64, 96),
        TrophyFish.STEAMING_HOT_FLOUNDER to intArrayOf(20, 28, 40, 60),
        TrophyFish.SULPHUR_SKITTER to intArrayOf(40, 60, 80, 120),
        TrophyFish.VANILLE to intArrayOf(80, 120, 160, 240),
        TrophyFish.VOLCANIC_STONEFISH to intArrayOf(20, 28, 40, 60),
        TrophyFish.OBFUSCATED_1 to intArrayOf(16, 24, 32, 48),
        TrophyFish.OBFUSCATED_2 to intArrayOf(40, 60, 80, 120),
        TrophyFish.OBFUSCATED_3 to intArrayOf(400, 700, 1000, 1300)
    )

    private val FROG_DONATION_REWARDS = mapOf(
        TrophyFrog.COMMON_FROG to intArrayOf(8, 16, 32, 64),
        TrophyFrog.EXPLODING_FROG to intArrayOf(12, 24, 48, 96),
        TrophyFrog.LEAP_FROG to intArrayOf(24, 48, 96, 192),
        TrophyFrog.WETLANDS_FROG to intArrayOf(20, 40, 80, 160),
        TrophyFrog.REALITY_HOPPER to intArrayOf(20, 40, 80, 160),
        TrophyFrog.BLESSED_FROG to intArrayOf(32, 64, 128, 256),
        TrophyFrog.BULLFROG to intArrayOf(40, 80, 160, 320),
        TrophyFrog.SEA_FROG to intArrayOf(40, 80, 160, 320),
        TrophyFrog.CAVE_FROG to intArrayOf(80, 160, 320, 640),
        TrophyFrog.HIGHLANDS_FROG to intArrayOf(80, 160, 320, 640),
        TrophyFrog.TREE_FROG to intArrayOf(80, 160, 320, 640),
        TrophyFrog.PUDDLE_JUMPER to intArrayOf(128, 192, 256, 512)
    )

    fun getFilletReward(fish: TrophyFish, tier: TrophyTier): Int {
        val rewards = FISH_FILLET_REWARDS[fish] ?: return 0
        return rewards.getOrElse(tier.ordinal) { 0 }
    }

    fun getDonationReward(frog: TrophyFrog, tier: TrophyTier): Int {
        val rewards = FROG_DONATION_REWARDS[frog] ?: return 0
        return rewards.getOrElse(tier.ordinal) { 0 }
    }
}

fun TrophyFish.getFilletReward(tier: TrophyTier): Int = TrophySackRewards.getFilletReward(this, tier)

fun TrophyFrog.getDonationReward(tier: TrophyTier): Int = TrophySackRewards.getDonationReward(this, tier)

val TrophyTier.color: TextColor
    get() = when (this) {
        TrophyTier.BRONZE -> TextColor.DARK_GRAY
        TrophyTier.SILVER -> TextColor.WHITE
        TrophyTier.GOLD -> TextColor.GOLD
        TrophyTier.DIAMOND -> TextColor.AQUAMARINE
    }

val TrophyTier.displayName: String
    get() = when (this) {
        TrophyTier.BRONZE -> "Bronze"
        TrophyTier.SILVER -> "Silver"
        TrophyTier.GOLD -> "Gold"
        TrophyTier.DIAMOND -> "Diamond"
    }
