package cloud.glitchdev.rfu.data.drops

import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.data.drops.migration.DropMigration
import cloud.glitchdev.rfu.events.managers.DropEvents
import cloud.glitchdev.rfu.events.managers.CocoonEvents.registerCocoonEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.utils.JsonFile
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import com.google.gson.JsonDeserializer

@AutoRegister
object DropManager : RegisteredEvent {
    init {
        // JsonFile.reloadAll must restore catches before migrating a recovered drops file.
        CatchTracker.catchesFile
    }

    val dropsFile: JsonFile<DropHistory> = JsonFile(
        filename = "drops.json",
        type = DropHistory::class.java,
        defaultFactory = { DropHistory() },
        builder = { builder ->
            // Skip entries whose enum value no longer exists instead of throwing and wiping the entire file.
            builder.registerTypeAdapter(DropHistory::class.java,
                JsonDeserializer { json, _, context ->
                    val root = json.asJsonObject
                    DropMigration.runMigrations(root)
                    DropHistory().apply {
                        version = root[DropMigration.VERSION_KEY].asInt
                        drops = root["drops"]?.takeIf { it.isJsonArray }?.asJsonArray
                            ?.mapNotNull { context.deserialize<DropHistory.DropEntry>(it, DropHistory.DropEntry::class.java) }
                            ?.toMutableList() ?: mutableListOf()
                        dyeDrops = root["dyeDrops"]?.takeIf { it.isJsonArray }?.asJsonArray
                            ?.mapNotNull { context.deserialize<DropHistory.DyeDropEntry>(it, DropHistory.DyeDropEntry::class.java) }
                            ?.toMutableList() ?: mutableListOf()
                    }
                }
            ).registerTypeAdapter(DropHistory.DropEntry::class.java,
                JsonDeserializer { json, _, context ->
                    val obj = json.asJsonObject
                    val typeName = obj["type"]?.asString ?: return@JsonDeserializer null
                    val drop = runCatching {
                        enumValueOf<RareDrops>(typeName)
                    }.getOrNull() ?: return@JsonDeserializer null
                    val entry = DropHistory.DropEntry(drop)
                    entry.currentCount = obj["currentCount"]?.takeUnless { it.isJsonNull }?.asInt ?: 0
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
                    entry.currentCount = obj["currentCount"]?.takeUnless { it.isJsonNull }?.asInt ?: 0
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

    val dropHistory: DropHistory get() = dropsFile.data

    override fun register() {
        registerSeaCreatureCatchEvent(0) { sc, doubleHook, _, _, _ ->
            dropHistory.registerCatch(sc, if (doubleHook) 2 else 1)
        }
        registerCocoonEvent(0) { sc ->
            dropHistory.registerCatch(sc)
        }

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
