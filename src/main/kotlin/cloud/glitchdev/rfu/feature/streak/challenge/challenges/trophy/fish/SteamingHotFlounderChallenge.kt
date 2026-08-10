package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object SteamingHotFlounderChallenge : BaseTrophyFishChallenge(
    id = "tf_steaming_hot_flounder",
    trophyFish = TrophyFish.STEAMING_HOT_FLOUNDER,
    title = "Flounder Catch"
)
