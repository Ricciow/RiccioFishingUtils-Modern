package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object VolcanicStonefishChallenge : BaseTrophyFishChallenge(
    id = "tf_volcanic_stonefish",
    trophyFish = TrophyFish.VOLCANIC_STONEFISH,
    title = "Volcanic Stonefish"
)
