package cloud.glitchdev.rfu.manager.drops

import cloud.glitchdev.rfu.events.managers.DropEvents
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.utils.JsonFile
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent

@AutoRegister
object DropManager : RegisteredEvent {
    val dropsFile = JsonFile(
        filename = "drops.json",
        type = DropHistory::class.java,
        defaultFactory = { DropHistory() }
    )

    val dropHistory = dropsFile.data

    override fun register() {
        DropEvents.registerRareDropEvent(0) { rareDrop, magicFind ->
            dropHistory.registerDrop(rareDrop, magicFind)
        }

        registerJoinEvent {
            dropsFile.save()
        }

        registerShutdownEvent {
            dropsFile.save()
        }
    }
}