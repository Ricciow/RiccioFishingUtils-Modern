package cloud.glitchdev.rfu.feature.streak.challenge.challenges.island

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object DesertIslandCatchChallenge : BaseIslandCatchChallenge(
    id = "sc_island_desert",
    island = FishingIslands.DESERT,
    title = "Oasis Angler"
)
