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

@AutoRegister
object TrophyCatchEvents : RegisteredEvent {
    private val TROPHY_FROG_REGEX = """ TROPHY FROG! You caught (?:an? )?(.+?) (BRONZE|SILVER|GOLD|DIAMOND)!""".toRegex(RegexOption.IGNORE_CASE)
    private val TROPHY_FISH_REGEX = """ TROPHY FISH! You caught (?:an? )?(.+?) (BRONZE|SILVER|GOLD|DIAMOND)!""".toRegex(RegexOption.IGNORE_CASE)

    override fun register() {
        registerGameEvent(TROPHY_FROG_REGEX) { _, _, matches ->
            val name = matches?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            val tierStr = matches.groupValues.getOrNull(2)?.uppercase() ?: return@registerGameEvent
            val tier = TrophyTier.entries.find { it.name == tierStr } ?: return@registerGameEvent
            val frog = TrophyFrog.fromName(name) ?: return@registerGameEvent
            TrophyFrogCatchEventManager.runTasks(frog, tier)
        }

        registerGameEvent(TROPHY_FISH_REGEX) { _, _, matches ->
            val name = matches?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            val tierStr = matches.groupValues.getOrNull(2)?.uppercase() ?: return@registerGameEvent
            val tier = TrophyTier.entries.find { it.name == tierStr } ?: return@registerGameEvent
            val fish = TrophyFish.fromName(name) ?: return@registerGameEvent
            TrophyFishCatchEventManager.runTasks(fish, tier)
        }
    }

    fun registerTrophyFrogCatchEvent(
        priority: Int = 20,
        callback: (frog: TrophyFrog, tier: TrophyTier) -> Unit
    ): TrophyFrogCatchEventManager.TrophyFrogCatchEvent {
        return TrophyFrogCatchEventManager.register(priority, callback)
    }

    fun registerTrophyFishCatchEvent(
        priority: Int = 20,
        callback: (fish: TrophyFish, tier: TrophyTier) -> Unit
    ): TrophyFishCatchEventManager.TrophyFishCatchEvent {
        return TrophyFishCatchEventManager.register(priority, callback)
    }

    fun registerTrophyCatchEvent(
        priority: Int = 20,
        callback: (trophy: Trophy, tier: TrophyTier, type: TrophyType) -> Unit
    ): Pair<TrophyFrogCatchEventManager.TrophyFrogCatchEvent, TrophyFishCatchEventManager.TrophyFishCatchEvent> {
        val frogEvent = registerTrophyFrogCatchEvent(priority) { frog, tier ->
            callback(frog, tier, TrophyType.FROG)
        }
        val fishEvent = registerTrophyFishCatchEvent(priority) { fish, tier ->
            callback(fish, tier, TrophyType.FISH)
        }
        return Pair(frogEvent, fishEvent)
    }

    object TrophyFrogCatchEventManager : AbstractEventManager<(frog: TrophyFrog, tier: TrophyTier) -> Unit, TrophyFrogCatchEventManager.TrophyFrogCatchEvent>() {
        override val runTasks: (frog: TrophyFrog, tier: TrophyTier) -> Unit = { frog, tier ->
            safeExecution {
                tasks.forEach { task -> task.callback(frog, tier) }
            }
        }

        fun register(priority: Int = 20, callback: (frog: TrophyFrog, tier: TrophyTier) -> Unit): TrophyFrogCatchEvent {
            return TrophyFrogCatchEvent(priority, callback).register()
        }

        class TrophyFrogCatchEvent(
            priority: Int = 20,
            callback: (frog: TrophyFrog, tier: TrophyTier) -> Unit
        ) : ManagedTask<(frog: TrophyFrog, tier: TrophyTier) -> Unit, TrophyFrogCatchEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object TrophyFishCatchEventManager : AbstractEventManager<(fish: TrophyFish, tier: TrophyTier) -> Unit, TrophyFishCatchEventManager.TrophyFishCatchEvent>() {
        override val runTasks: (fish: TrophyFish, tier: TrophyTier) -> Unit = { fish, tier ->
            safeExecution {
                tasks.forEach { task -> task.callback(fish, tier) }
            }
        }

        fun register(priority: Int = 20, callback: (fish: TrophyFish, tier: TrophyTier) -> Unit): TrophyFishCatchEvent {
            return TrophyFishCatchEvent(priority, callback).register()
        }

        class TrophyFishCatchEvent(
            priority: Int = 20,
            callback: (fish: TrophyFish, tier: TrophyTier) -> Unit
        ) : ManagedTask<(fish: TrophyFish, tier: TrophyTier) -> Unit, TrophyFishCatchEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
