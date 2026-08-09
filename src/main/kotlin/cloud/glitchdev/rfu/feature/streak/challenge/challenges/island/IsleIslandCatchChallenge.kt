package cloud.glitchdev.rfu.feature.streak.challenge.challenges.island

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object IsleIslandCatchChallenge : BaseIslandCatchChallenge(
    id = "sc_island_isle",
    island = FishingIslands.ISLE,
    title = "Crimson Isle Hunter"
)
