package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.utils.dsl.removeFormatting
import cloud.glitchdev.rfu.utils.dsl.removeRankTag

@AutoRegister
object DropEvents : AbstractEventManager<(rareDrop: RareDrops, magicFind: Int?) -> Unit, DropEvents.RareDropEvent>(), RegisteredEvent {
    override fun register() {
        ChatEvents.registerAllowGameEvent { text, overlay, _ ->
            if (overlay) return@registerAllowGameEvent true
            val string = text.string.removeFormatting()

            GeneralFishing.RARE_DROP_REGEX.find(string)?.groupValues?.let { (_, dropName, mfString) ->
                val rareDrop = RareDrops.getRelatedDrop(dropName) ?: return@let
                val magicFind = mfString.toIntOrNull()

                runTasks(rareDrop, magicFind)

                if (GeneralFishing.customRareDropMessage) return@registerAllowGameEvent false
            }

            GeneralFishing.DYE_REGEX.find(string)?.groupValues?.let { (_, username, dropName) ->
                if (!username.removeRankTag().isUser()) return@let
                val dyeDrop = RareDrops.getRelatedDrop(dropName) ?: return@let

                runTasks(dyeDrop, null)

                if (GeneralFishing.customRareDropMessage) return@registerAllowGameEvent false
            }
            true
        }
    }

    private fun runTasks(rareDrop: RareDrops, magicFind: Int?) {
        tasks.forEach { event -> event.callback(rareDrop, magicFind) }
    }

    fun registerRareDropEvent(
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