package cloud.glitchdev.rfu.feature.streak.challenge.challenges.island

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object DwarvenIslandCatchChallenge : BaseIslandCatchChallenge(
    id = "sc_island_dwarven",
    island = FishingIslands.DWARVEN,
    title = "Dwarven Angler"
) {
    override val weight: Int = 5
}
