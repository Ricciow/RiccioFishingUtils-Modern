package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.constants.fishing.TrophyTier
import cloud.glitchdev.rfu.constants.fishing.TrophyType
import cloud.glitchdev.rfu.constants.fishing.Trophy
import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@AutoRegister
object TrophyCatchEvents : RegisteredEvent {
    val TROPHY_FROG_REGEX = """(?:\s*)?TROPHY FROG! You caught (?:an? )?(.+?) (BRONZE|SILVER|GOLD|DIAMOND)(?:\s+x(\d+))?!""".toExactRegex()
    val TROPHY_FISH_REGEX = """(?:\s*)?TROPHY FISH! You caught (?:an? )?(.+?) (BRONZE|SILVER|GOLD|DIAMOND)(?:\s+x(\d+))?!""".toExactRegex()

    override fun register() {
        registerGameEvent(TROPHY_FROG_REGEX) { _, _, matches ->
            val name = matches?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            val tierStr = matches.groupValues.getOrNull(2)?.uppercase() ?: return@registerGameEvent
            val tier = TrophyTier.entries.find { it.name == tierStr } ?: return@registerGameEvent
            val amount = matches.groupValues.getOrNull(3)?.toIntOrNull() ?: 1
            val frog = TrophyFrog.fromName(name) ?: return@registerGameEvent
            TrophyFrogCatchEventManager.runTasks(frog, tier, amount)
        }

        registerGameEvent(TROPHY_FISH_REGEX) { _, _, matches ->
            val name = matches?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            val tierStr = matches.groupValues.getOrNull(2)?.uppercase() ?: return@registerGameEvent
            val tier = TrophyTier.entries.find { it.name == tierStr } ?: return@registerGameEvent
            val amount = matches.groupValues.getOrNull(3)?.toIntOrNull() ?: 1
            val fish = TrophyFish.fromName(name) ?: return@registerGameEvent
            TrophyFishCatchEventManager.runTasks(fish, tier, amount)
        }
    }

    fun registerTrophyFrogCatchEvent(
        priority: Int = 20,
        callback: (frog: TrophyFrog, tier: TrophyTier, amount: Int) -> Unit
    ): TrophyFrogCatchEventManager.TrophyFrogCatchEvent {
        return TrophyFrogCatchEventManager.register(priority, callback)
    }

    fun registerTrophyFishCatchEvent(
        priority: Int = 20,
        callback: (fish: TrophyFish, tier: TrophyTier, amount: Int) -> Unit
    ): TrophyFishCatchEventManager.TrophyFishCatchEvent {
        return TrophyFishCatchEventManager.register(priority, callback)
    }

    fun registerTrophyCatchEvent(
        priority: Int = 20,
        callback: (trophy: Trophy, tier: TrophyTier, type: TrophyType, amount: Int) -> Unit
    ): Pair<TrophyFrogCatchEventManager.TrophyFrogCatchEvent, TrophyFishCatchEventManager.TrophyFishCatchEvent> {
        val frogEvent = registerTrophyFrogCatchEvent(priority) { frog, tier, amount ->
            callback(frog, tier, TrophyType.FROG, amount)
        }
        val fishEvent = registerTrophyFishCatchEvent(priority) { fish, tier, amount ->
            callback(fish, tier, TrophyType.FISH, amount)
        }
        return Pair(frogEvent, fishEvent)
    }

    object TrophyFrogCatchEventManager : AbstractEventManager<(frog: TrophyFrog, tier: TrophyTier, amount: Int) -> Unit, TrophyFrogCatchEventManager.TrophyFrogCatchEvent>() {
        override val runTasks: (frog: TrophyFrog, tier: TrophyTier, amount: Int) -> Unit = { frog, tier, amount ->
            safeExecution {
                tasks.forEach { task -> task.callback(frog, tier, amount) }
            }
        }

        fun register(priority: Int = 20, callback: (frog: TrophyFrog, tier: TrophyTier, amount: Int) -> Unit): TrophyFrogCatchEvent {
            return TrophyFrogCatchEvent(priority, callback).register()
        }

        class TrophyFrogCatchEvent(
            priority: Int = 20,
            callback: (frog: TrophyFrog, tier: TrophyTier, amount: Int) -> Unit
        ) : ManagedTask<(frog: TrophyFrog, tier: TrophyTier, amount: Int) -> Unit, TrophyFrogCatchEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object TrophyFishCatchEventManager : AbstractEventManager<(fish: TrophyFish, tier: TrophyTier, amount: Int) -> Unit, TrophyFishCatchEventManager.TrophyFishCatchEvent>() {
        override val runTasks: (fish: TrophyFish, tier: TrophyTier, amount: Int) -> Unit = { fish, tier, amount ->
            safeExecution {
                tasks.forEach { task -> task.callback(fish, tier, amount) }
            }
        }

        fun register(priority: Int = 20, callback: (fish: TrophyFish, tier: TrophyTier, amount: Int) -> Unit): TrophyFishCatchEvent {
            return TrophyFishCatchEvent(priority, callback).register()
        }

        class TrophyFishCatchEvent(
            priority: Int = 20,
            callback: (fish: TrophyFish, tier: TrophyTier, amount: Int) -> Unit
        ) : ManagedTask<(fish: TrophyFish, tier: TrophyTier, amount: Int) -> Unit, TrophyFishCatchEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
