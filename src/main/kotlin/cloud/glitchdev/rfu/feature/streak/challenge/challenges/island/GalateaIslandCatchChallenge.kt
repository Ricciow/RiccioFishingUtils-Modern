package cloud.glitchdev.rfu.feature.streak.challenge.challenges.island

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object GalateaIslandCatchChallenge : BaseIslandCatchChallenge(
    id = "sc_island_galatea",
    island = FishingIslands.GALATEA,
    title = "Galatea Angler"
)
