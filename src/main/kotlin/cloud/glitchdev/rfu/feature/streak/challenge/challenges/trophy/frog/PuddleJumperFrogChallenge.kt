package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.frog

import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object PuddleJumperFrogChallenge : BaseTrophyFrogChallenge(
    id = "tfr_puddle_jumper",
    trophyFrog = TrophyFrog.PUDDLE_JUMPER,
    title = "Puddle Jumper Frog"
)
