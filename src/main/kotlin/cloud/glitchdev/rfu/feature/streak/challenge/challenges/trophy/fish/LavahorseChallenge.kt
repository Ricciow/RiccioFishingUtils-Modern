package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object LavahorseChallenge : BaseTrophyFishChallenge(
    id = "tf_lavahorse",
    trophyFish = TrophyFish.LAVAHORSE,
    title = "Lavahorse Catch"
)
