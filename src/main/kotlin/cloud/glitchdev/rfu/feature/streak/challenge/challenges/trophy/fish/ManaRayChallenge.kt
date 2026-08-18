package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object ManaRayChallenge : BaseTrophyFishChallenge(
    id = "tf_mana_ray",
    trophyFish = TrophyFish.MANA_RAY,
    title = "Mana Ray Catch"
)
