package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.frog

import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object BlessedFrogChallenge : BaseTrophyFrogChallenge(
    id = "tfr_blessed_frog",
    trophyFrog = TrophyFrog.BLESSED_FROG,
    title = "Blessed Frog"
)
