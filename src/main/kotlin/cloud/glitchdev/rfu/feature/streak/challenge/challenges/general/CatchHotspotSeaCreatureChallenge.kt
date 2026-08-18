package cloud.glitchdev.rfu.feature.streak.challenge.challenges.general

import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object CatchHotspotSeaCreatureChallenge : BaseChallenge() {
    override val id: String = "catch_hotspot_sea_creature"
    override val title: String = "Hotspot Hunter"
    override val description: String = "Catch sea creatures in Hotspots."
    override val weight: Int = 50

    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 14 -> 25
            streakDays < 28 -> 40
            else -> 60
        }
    }

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        return "Catch $target sea creatures in Hotspots."
    }

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { _, doubleHook, hotspot, _, _ ->
            if (hotspot != null) {
                addProgress(if (doubleHook) 2 else 1)
            }
        })
    }
}
