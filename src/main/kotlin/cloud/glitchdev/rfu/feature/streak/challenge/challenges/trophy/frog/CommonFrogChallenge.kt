package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.frog

import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object CommonFrogChallenge : BaseTrophyFrogChallenge(
    id = "tfr_common_frog",
    trophyFrog = TrophyFrog.COMMON_FROG,
    title = "Common Frog Catch"
)
