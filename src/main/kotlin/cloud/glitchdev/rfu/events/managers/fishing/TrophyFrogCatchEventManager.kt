package cloud.glitchdev.rfu.events.managers.fishing

import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.constants.fishing.TrophyTier
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object TrophyFrogCatchEventManager : AbstractEventManager<(frog: TrophyFrog, tier: TrophyTier, amount: Int) -> Unit, TrophyFrogCatchEventManager.TrophyFrogCatchEvent>() {
    override val runTasks: (frog: TrophyFrog, tier: TrophyTier, amount: Int) -> Unit = { frog, tier, amount ->
        safeExecution {
            tasks.forEach { task -> task.callback(frog, tier, amount) }
        }
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (frog: TrophyFrog, tier: TrophyTier, amount: Int) -> Unit): TrophyFrogCatchEvent {
        return TrophyFrogCatchEvent(priority, callback).register()
    }

    class TrophyFrogCatchEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (frog: TrophyFrog, tier: TrophyTier, amount: Int) -> Unit
    ) : ManagedTask<(frog: TrophyFrog, tier: TrophyTier, amount: Int) -> Unit, TrophyFrogCatchEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
