package cloud.glitchdev.rfu.feature.streak.challenge.challenges.drops

import cloud.glitchdev.rfu.events.managers.LootshareEvents.registerLootshareEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge
import gg.essential.universal.utils.toUnformattedString

@RFUChallenge
object LootshareMagmaLordFragmentChallenge : BaseChallenge() {
    override val id: String = "lootshare_magma_lord_fragment"
    override val title: String = "Magma Lord Collector"
    override val description: String = "Lootshare a Magma Lord Fragment."
    override val weight: Int = 20

    override fun getTargetProgress(streakDays: Int): Int = 1

    override fun setupListeners() {
        activeListeners.add(registerLootshareEvent { _, items ->
            val hasFrag = items.any { item ->
                item.itemStack.customName?.toUnformattedString()?.contains("Magma Lord Fragment", ignoreCase = true) == true ||
                item.itemStack.hoverName.string.contains("Magma Lord Fragment", ignoreCase = true)
            }
            if (hasFrag) {
                addProgress(1)
            }
        })
    }
}
