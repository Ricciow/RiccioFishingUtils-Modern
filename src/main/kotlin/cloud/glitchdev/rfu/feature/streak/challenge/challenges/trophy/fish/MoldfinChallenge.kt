package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object MoldfinChallenge : BaseTrophyFishChallenge(
    id = "tf_moldfin",
    trophyFish = TrophyFish.MOLDFIN,
    title = "Moldfin Hunter"
)
