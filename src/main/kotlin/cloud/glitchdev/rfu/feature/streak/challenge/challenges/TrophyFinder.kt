package cloud.glitchdev.rfu.feature.streak.challenge.challenges

import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object TrophyFinder : BaseChallenge() {
    override val id: String = "trophy_finder"
    override val title: String = "Trophy Finder"
    override val description: String = "Fish up trophy fishes/frogs"

    override fun getTargetProgress(streakDays: Int): Int {
        return 1
    }
}