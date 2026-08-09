package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.frog

import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object CaveFrogChallenge : BaseTrophyFrogChallenge(
    id = "tfr_cave_frog",
    trophyFrog = TrophyFrog.CAVE_FROG,
    title = "Cave Frog Catch"
)
