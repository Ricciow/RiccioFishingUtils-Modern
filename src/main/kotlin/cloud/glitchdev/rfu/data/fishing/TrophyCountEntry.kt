package cloud.glitchdev.rfu.data.fishing

import cloud.glitchdev.rfu.constants.fishing.TrophyTier

data class TrophyCountEntry(
    val name: String,
    var bronze: Int = 0,
    var silver: Int = 0,
    var gold: Int = 0,
    var diamond: Int = 0
) {
    val total: Int get() = bronze + silver + gold + diamond

    fun addCatch(tier: TrophyTier, amount: Int) {
        when (tier) {
            TrophyTier.BRONZE -> bronze += amount
            TrophyTier.SILVER -> silver += amount
            TrophyTier.GOLD -> gold += amount
            TrophyTier.DIAMOND -> diamond += amount
        }
    }
}
