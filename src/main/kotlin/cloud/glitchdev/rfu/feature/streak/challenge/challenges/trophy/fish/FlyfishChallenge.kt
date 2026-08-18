package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object FlyfishChallenge : BaseTrophyFishChallenge(
    id = "tf_flyfish",
    trophyFish = TrophyFish.FLYFISH,
    title = "Flyfish Catch"
)
