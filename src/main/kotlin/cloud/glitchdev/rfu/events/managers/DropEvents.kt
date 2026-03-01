package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.constants.Dyes
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.utils.dsl.removeFormatting
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@AutoRegister
object DropEvents : RegisteredEvent {
    val DYE_ODD_REGEX = """.+ \d+\/\d+(?:\.\d+)?\w? \(\d+(?:\.\d+)?%\) chance! \(\+(\d+)% ✯ Magic Find\)""".toExactRegex()
    var currentDye : Dyes? = null

    override fun register() {
        ChatEvents.registerAllowGameEvent { text, overlay, _ ->
            if (overlay) return@registerAllowGameEvent true
            val string = text.string.removeFormatting()

            GeneralFishing.RARE_DROP_REGEX.find(string)?.groupValues?.let { (_, dropName, mfString) ->
                val rareDrop = RareDrops.getRelatedDrop(dropName) ?: return@let
                val magicFind = mfString.toIntOrNull()

                RareDropEventManager.runTasks(rareDrop, magicFind)

                if (GeneralFishing.customRareDropMessage) return@registerAllowGameEvent false
            }

            GeneralFishing.DYE_REGEX.find(string)?.groupValues?.let { (_, username, dropName) ->
                if (!username.removeRankTag().isUser()) return@let
                currentDye = Dyes.getRelatedDye(dropName) ?: return@let
            }

            DYE_ODD_REGEX.find(string)?.groupValues?.let { (_, mfString) ->
                val magicFind = mfString.toIntOrNull()
                DyeDropEventManager.runTasks(currentDye ?: return@let, magicFind)
            }

            true
        }
    }

    fun registerRareDropEvent(
        priority: Int = 20,
        callback: (rareDrop: RareDrops, magicFind: Int?) -> Unit
    ): RareDropEventManager.RareDropEvent {
        return RareDropEventManager.register(priority, callback)
    }

    fun registerDyeDropEvent(
        priority: Int = 20,
        callback: (dyeDrop: Dyes, magicFind : Int?) -> Unit
    ): DyeDropEventManager.DyeDropEvent {
        return DyeDropEventManager.register(priority, callback)
    }

    object RareDropEventManager : AbstractEventManager<(rareDrop: RareDrops, magicFind: Int?) -> Unit, RareDropEventManager.RareDropEvent>() {
        fun runTasks(rareDrop: RareDrops, magicFind: Int?) {
            safeExecution {
                tasks.forEach { event -> event.callback(rareDrop, magicFind) }
            }
        }

        fun register(
            priority: Int = 20,
            callback: (rareDrop: RareDrops, magicFind: Int?) -> Unit
        ): RareDropEvent {
            return RareDropEvent(priority, callback).register()
        }

        class RareDropEvent(
            priority: Int = 20,
            callback: (rareDrop: RareDrops, magicFind: Int?) -> Unit
        ) : ManagedTask<(rareDrop: RareDrops, magicFind: Int?) -> Unit, RareDropEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object DyeDropEventManager : AbstractEventManager<(dyeDrop: Dyes, magicFind : Int?) -> Unit, DyeDropEventManager.DyeDropEvent>() {
        fun runTasks(dyeDrop: Dyes, magicFind : Int?) {
            safeExecution {
                tasks.forEach { event -> event.callback(dyeDrop, magicFind) }
            }
        }

        fun register(
            priority: Int = 20,
            callback: (dyeDrop: Dyes, magicFind : Int?) -> Unit
        ): DyeDropEvent {
            return DyeDropEvent(priority, callback).register()
        }

        class DyeDropEvent(
            priority: Int = 20,
            callback: (dyeDrop: Dyes, magicFind : Int?) -> Unit
        ) : ManagedTask<(dyeDrop: Dyes, magicFind : Int?) -> Unit, DyeDropEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}