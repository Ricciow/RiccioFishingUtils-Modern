package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.frog

import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object TreeFrogChallenge : BaseTrophyFrogChallenge(
    id = "tfr_tree_frog",
    trophyFrog = TrophyFrog.TREE_FROG,
    title = "Tree Frog Catch"
)
