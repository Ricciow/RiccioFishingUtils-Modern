package cloud.glitchdev.rfu.manager.drops

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.events.managers.WorldChangeEvents.registerWorldChangeEvent
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.JsonFile
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.utils.dsl.removeFormatting
import cloud.glitchdev.rfu.utils.dsl.removeRankTag

@AutoRegister
object DropManager : RegisteredEvent {
    val dropsFile = JsonFile(
        filename = "drops.json",
        type = DropHistory::class.java,
        defaultFactory = { DropHistory() }
    )

    val dropHistory = dropsFile.data

    override fun register() {
        Chat.registerChat { text ->
            val string = text.string.removeFormatting()

            GeneralFishing.RARE_DROP_REGEX.find(string)?.groupValues?.let { (_, dropName, mfString) ->
                val rareDrop = RareDrops.getRelatedDrop(dropName) ?: return@let
                val magicFind = mfString.toIntOrNull()

                dropHistory.registerDrop(rareDrop, magicFind)
            }

            GeneralFishing.DYE_REGEX.find(string)?.groupValues?.let { (_, username, dropName) ->
                if (!username.removeRankTag().isUser()) return@let
                val dyeDrop = RareDrops.getRelatedDrop(dropName) ?: return@let

                dropHistory.registerDrop(dyeDrop)
            }
        }

        registerWorldChangeEvent {
            dropsFile.save()
        }

        registerShutdownEvent {
            dropsFile.save()
        }
    }
}