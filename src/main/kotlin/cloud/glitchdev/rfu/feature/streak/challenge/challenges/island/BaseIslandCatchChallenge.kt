package cloud.glitchdev.rfu.feature.streak.challenge.challenges.island

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.utils.World

abstract class BaseIslandCatchChallenge(
    override val id: String,
    val island: FishingIslands,
    override val title: String
) : BaseChallenge() {
    override val weight: Int = 20

    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 14 -> 40
            streakDays < 28 -> 70
            else -> 100
        }
    }

    override fun getTitle(streakDays: Int): String = title

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        return "Catch $target sea creatures on ${island.island}."
    }

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { _, doubleHook, _, _, _ ->
            if (World.island == island) {
                addProgress(if (doubleHook) 2 else 1)
            }
        })
    }
}
