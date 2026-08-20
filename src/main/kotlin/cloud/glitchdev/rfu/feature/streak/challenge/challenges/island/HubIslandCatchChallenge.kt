package cloud.glitchdev.rfu.feature.streak.challenge.challenges.island

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object HubIslandCatchChallenge : BaseIslandCatchChallenge(
    id = "sc_island_hub",
    island = FishingIslands.HUB,
    title = "Hub Angler"
) {
    override val weight: Int = 5
}
