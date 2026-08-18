package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.frog

import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object SeaFrogChallenge : BaseTrophyFrogChallenge(
    id = "tfr_sea_frog",
    trophyFrog = TrophyFrog.SEA_FROG,
    title = "Sea Frog Catch"
)
