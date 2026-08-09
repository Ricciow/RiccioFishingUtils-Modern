package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object GusherChallenge : BaseTrophyFishChallenge(
    id = "tf_gusher",
    trophyFish = TrophyFish.GUSHER,
    title = "Gusher Catch"
)
