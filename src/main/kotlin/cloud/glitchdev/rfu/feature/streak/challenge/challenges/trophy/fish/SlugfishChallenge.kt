package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object SlugfishChallenge : BaseTrophyFishChallenge(
    id = "tf_slugfish",
    trophyFish = TrophyFish.SLUGFISH,
    title = "Slugfish Hunter"
)
