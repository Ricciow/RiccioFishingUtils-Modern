package cloud.glitchdev.rfu.feature.streak.challenge.challenges.island

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object AtollIslandCatchChallenge : BaseIslandCatchChallenge(
    id = "sc_island_atoll",
    island = FishingIslands.ATOLL,
    title = "Lotus Atoll Angler"
)
