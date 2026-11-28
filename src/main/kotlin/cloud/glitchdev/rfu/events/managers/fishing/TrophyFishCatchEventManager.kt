package cloud.glitchdev.rfu.events.managers.fishing

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.fishing.TrophyTier
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object TrophyFishCatchEventManager : AbstractEventManager<(fish: TrophyFish, tier: TrophyTier, amount: Int) -> Unit, TrophyFishCatchEventManager.TrophyFishCatchEvent>() {
    override val runTasks: (fish: TrophyFish, tier: TrophyTier, amount: Int) -> Unit = { fish, tier, amount ->
        safeExecution {
            tasks.forEach { task -> task.callback(fish, tier, amount) }
        }
    }

    class TrophyFishCatchEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (fish: TrophyFish, tier: TrophyTier, amount: Int) -> Unit
    ) : ManagedTask<(fish: TrophyFish, tier: TrophyTier, amount: Int) -> Unit, TrophyFishCatchEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
