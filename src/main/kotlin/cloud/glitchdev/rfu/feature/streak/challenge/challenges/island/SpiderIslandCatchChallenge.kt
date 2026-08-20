package cloud.glitchdev.rfu.feature.streak.challenge.challenges.island

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object SpiderIslandCatchChallenge : BaseIslandCatchChallenge(
    id = "sc_island_spider",
    island = FishingIslands.SPIDER,
    title = "Spider's Den Angler"
) {
    override val weight: Int = 5
}
