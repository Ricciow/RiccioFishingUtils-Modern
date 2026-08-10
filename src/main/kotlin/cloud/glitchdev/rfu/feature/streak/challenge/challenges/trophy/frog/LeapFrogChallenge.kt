package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.frog

import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object LeapFrogChallenge : BaseTrophyFrogChallenge(
    id = "tfr_leap_frog",
    trophyFrog = TrophyFrog.LEAP_FROG,
    title = "Leap Frog Catch"
)
