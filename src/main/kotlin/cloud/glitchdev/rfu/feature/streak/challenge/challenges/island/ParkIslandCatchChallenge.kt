package cloud.glitchdev.rfu.feature.streak.challenge.challenges.island

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object ParkIslandCatchChallenge : BaseIslandCatchChallenge(
    id = "sc_island_park",
    island = FishingIslands.PARK,
    title = "Park Angler"
)
