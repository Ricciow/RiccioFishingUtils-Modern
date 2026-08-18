package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object SkeletonFishChallenge : BaseTrophyFishChallenge(
    id = "tf_skeleton_fish",
    trophyFish = TrophyFish.SKELETON_FISH,
    title = "Skeleton Fish"
)
