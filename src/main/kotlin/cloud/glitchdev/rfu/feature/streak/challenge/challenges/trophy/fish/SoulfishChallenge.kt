package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object SoulfishChallenge : BaseTrophyFishChallenge(
    id = "tf_soulfish",
    trophyFish = TrophyFish.SOULFISH,
    title = "Soulfish Catch"
)
