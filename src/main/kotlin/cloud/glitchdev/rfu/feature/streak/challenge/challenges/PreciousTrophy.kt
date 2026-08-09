package cloud.glitchdev.rfu.feature.streak.challenge.challenges

import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object PreciousTrophy : BaseChallenge() {
    override val id: String = "precious_trophy"
    override val title: String = "Precious Trophy"
    override val description: String = "Fish up a gold or diamond trophy fish/frog"

    override fun getTargetProgress(streakDays: Int): Int {
        return 1
    }
}