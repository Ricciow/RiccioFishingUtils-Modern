package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.frog

import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object BullfrogChallenge : BaseTrophyFrogChallenge(
    id = "tfr_bullfrog",
    trophyFrog = TrophyFrog.BULLFROG,
    title = "Bullfrog Catch"
)
