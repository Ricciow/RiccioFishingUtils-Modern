package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object GoldenFishChallenge : BaseTrophyFishChallenge(
    id = "tf_golden_fish",
    trophyFish = TrophyFish.GOLDEN_FISH,
    title = "Golden Touch"
)
