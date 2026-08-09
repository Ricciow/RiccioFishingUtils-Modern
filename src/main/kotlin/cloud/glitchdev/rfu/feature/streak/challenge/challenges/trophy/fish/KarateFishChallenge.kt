package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object KarateFishChallenge : BaseTrophyFishChallenge(
    id = "tf_karate_fish",
    trophyFish = TrophyFish.KARATE_FISH,
    title = "Karate Master"
)
