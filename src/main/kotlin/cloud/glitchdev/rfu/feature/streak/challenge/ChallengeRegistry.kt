package cloud.glitchdev.rfu.feature.streak.challenge

import java.util.Random

object ChallengeRegistry {
    private val challengesMap = mutableMapOf<String, BaseChallenge>()

    fun register(challenge: BaseChallenge) {
        challengesMap[challenge.id] = challenge
    }

    fun getMandatoryChallenge(): BaseChallenge? {
        return challengesMap.values.find { it.isMandatoryBase } ?: challengesMap.values.firstOrNull()
    }

    fun getPoolChallenges(): List<BaseChallenge> {
        return challengesMap.values.filter { !it.isMandatoryBase }
    }

    fun getSeededPoolChallenges(dateSeed: Long, count: Int = 2): List<BaseChallenge> {
        val pool = getPoolChallenges()
        if (pool.isEmpty()) return emptyList()
        if (pool.size <= count) return pool
        val rng = Random(dateSeed)
        return pool.shuffled(rng).take(count)
    }

    fun getChallenge(id: String): BaseChallenge? = challengesMap[id]
}
