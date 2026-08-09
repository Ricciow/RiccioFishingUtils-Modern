package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object VanilleChallenge : BaseTrophyFishChallenge(
    id = "tf_vanille",
    trophyFish = TrophyFish.VANILLE,
    title = "Vanille Catch"
)
