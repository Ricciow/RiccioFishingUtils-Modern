package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.frog

import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object WetlandsFrogChallenge : BaseTrophyFrogChallenge(
    id = "tfr_wetlands_frog",
    trophyFrog = TrophyFrog.WETLANDS_FROG,
    title = "Wetlands Frog"
)
