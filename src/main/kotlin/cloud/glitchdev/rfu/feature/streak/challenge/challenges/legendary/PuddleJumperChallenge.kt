package cloud.glitchdev.rfu.feature.streak.challenge.challenges.legendary

import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object PuddleJumperChallenge : LegendarySeaCreatureChallenge(
    id = "sc_puddle_jumper",
    scName = "Puddle Jumper",
    title = "Puddle Jumper"
) {
    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 15 -> 3
            streakDays < 30 -> 6
            else -> 10
        }
    }
}
