package cloud.glitchdev.rfu.manager.drops

import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.events.managers.DropEvents
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.utils.JsonFile
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import com.google.gson.JsonDeserializer

@AutoRegister
object DropManager : RegisteredEvent {
    val dropsFile = JsonFile(
        filename = "drops.json",
        type = DropHistory::class.java,
        defaultFactory = { DropHistory() },
        builder = { builder ->
            // Skip entries whose enum value no longer exists instead of throwing and wiping the entire file.
            builder.registerTypeAdapter(DropHistory.DropEntry::class.java,
                JsonDeserializer { json, _, context ->
                    val obj = json.asJsonObject
                    val typeName = obj["type"]?.asString ?: return@JsonDeserializer null
                    val drop = runCatching {
                        enumValueOf<RareDrops>(typeName)
                    }.getOrNull() ?: return@JsonDeserializer null
                    val entry = DropHistory.DropEntry(drop)
                    obj["history"]?.asJsonArray?.forEach { el ->
                        val record = context.deserialize<DropRecord>(el, DropRecord::class.java)
                        if (record != null) entry.history.add(record)
                    }
                    entry
                }
            ).create()
        }
    )

    val dropHistory = dropsFile.data

    override fun register() {
        DropEvents.registerRareDropEvent(0) { rareDrop, magicFind ->
            dropHistory.registerDrop(rareDrop, magicFind)
        }

        DropEvents.registerDyeDropEvent(0) { dyeDrop, magicFind ->
            dropHistory.registerDrop(dyeDrop, magicFind)
        }

        registerJoinEvent {
            dropsFile.save()
        }

        registerShutdownEvent {
            dropsFile.save()
        }
    }
}