package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.frog

import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object ExplodingFrogChallenge : BaseTrophyFrogChallenge(
    id = "tfr_exploding_frog",
    trophyFrog = TrophyFrog.EXPLODING_FROG,
    title = "Exploding Frog"
)
