package cloud.glitchdev.rfu.feature.streak.challenge.challenges.island

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object BayouIslandCatchChallenge : BaseIslandCatchChallenge(
    id = "sc_island_bayou",
    island = FishingIslands.BAYOU,
    title = "Bayou Angler"
)
