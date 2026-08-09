package cloud.glitchdev.rfu.feature.streak.challenge.challenges.island

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object HollowsIslandCatchChallenge : BaseIslandCatchChallenge(
    id = "sc_island_hollows",
    island = FishingIslands.HOLLOWS,
    title = "Crystal Hollows Angler"
)
