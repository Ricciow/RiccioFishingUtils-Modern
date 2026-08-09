package cloud.glitchdev.rfu.feature.streak.challenge

import java.util.Random

object ChallengeRegistry {
    private val challengesMap = mutableMapOf<String, BaseChallenge>()

    fun register(challenge: BaseChallenge) {
        challengesMap[challenge.id] = challenge
    }

    fun getPoolChallenges(): List<BaseChallenge> {
        return challengesMap.values.toList()
    }

    fun getWeightedRandomChallenge(candidates: List<BaseChallenge>, rng: Random = Random()): BaseChallenge? {
        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates.first()

        val totalWeight = candidates.sumOf { it.weight.coerceAtLeast(1) }
        var roll = rng.nextInt(totalWeight)
        for (challenge in candidates) {
            val w = challenge.weight.coerceAtLeast(1)
            if (roll < w) {
                return challenge
            }
            roll -= w
        }
        return candidates.last()
    }

    fun getSeededPoolChallenges(dateSeed: Long, count: Int = 3): List<BaseChallenge> {
        val pool = getPoolChallenges().toMutableList()
        if (pool.isEmpty()) return emptyList()
        if (pool.size <= count) return pool

        val rng = Random(dateSeed)
        val selected = mutableListOf<BaseChallenge>()

        repeat(count) {
            val picked = getWeightedRandomChallenge(pool, rng) ?: return@repeat
            selected.add(picked)
            pool.remove(picked)
        }

        return selected
    }

    fun getChallenge(id: String): BaseChallenge? = challengesMap[id]
}
