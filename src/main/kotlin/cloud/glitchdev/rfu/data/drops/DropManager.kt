package cloud.glitchdev.rfu.data.drops

import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.events.managers.DropEvents
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
                        if (record != null) {
                            if (record.mobName == null && drop.relatedScs.size == 1) {
                                record.mobName = drop.relatedScs.first().scName
                            }
                            entry.history.add(record)
                        }
                    }
                    entry
                }
            ).registerTypeAdapter(DropHistory.DyeDropEntry::class.java,
                JsonDeserializer { json, _, context ->
                    val obj = json.asJsonObject
                    val typeName = obj["type"]?.asString ?: return@JsonDeserializer null
                    val drop = runCatching {
                        enumValueOf<Dyes>(typeName)
                    }.getOrNull() ?: return@JsonDeserializer null
                    val entry = DropHistory.DyeDropEntry(drop)
                    obj["history"]?.asJsonArray?.forEach { el ->
                        val record = context.deserialize<DropRecord>(el, DropRecord::class.java)
                        if (record != null) {
                            if (record.mobName == null && drop.relatedScs.size == 1) {
                                record.mobName = drop.relatedScs.first().scName
                            }
                            entry.history.add(record)
                        }
                    }
                    entry
                }
            ).create()
        },
        revertOnAlpha = true
    )

    val dropHistory get() = dropsFile.data

    override fun register() {
        DropEvents.registerRareDropEvent(0) { rareDrop, magicFind, mobName ->
            dropHistory.registerDrop(rareDrop, magicFind, mobName)
            dropsFile.save()
            true
        }

        DropEvents.registerDyeDropEvent(0) { dyeDrop, magicFind, mobName ->
            dropHistory.registerDrop(dyeDrop, magicFind, mobName)
            dropsFile.save()
        }
    }
}
