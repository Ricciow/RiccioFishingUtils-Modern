package cloud.glitchdev.rfu.feature.streak.challenge.challenges.mythic

import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge

abstract class MythicSeaCreatureChallenge(
    override val id: String,
    val scName: String,
    override val title: String
) : BaseChallenge() {
    override val weight: Int = 9

    private val creature by lazy { SeaCreatures.get(scName) }

    override fun getTargetProgress(streakDays: Int): Int {
        return if (streakDays < 20) 1 else 2
    }

    override fun getTitle(streakDays: Int): String = title

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        val displayName = creature?.scDisplayName ?: scName
        val plural = creature?.plural ?: "${displayName}s"
        return if (target == 1) {
            "Fish up 1 $displayName."
        } else {
            "Fish up $target $plural."
        }
    }

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { sc, doubleHook, _, _, _ ->
            val target = creature ?: return@registerSeaCreatureCatchEvent
            if (sc == target) {
                addProgress(if (doubleHook) 2 else 1)
            }
        })
    }
}
