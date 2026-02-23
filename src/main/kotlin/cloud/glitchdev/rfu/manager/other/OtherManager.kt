package cloud.glitchdev.rfu.manager.other

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.manager.other.data.Entry
import cloud.glitchdev.rfu.manager.other.data.StringEntry
import cloud.glitchdev.rfu.utils.JsonFile
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer

@AutoRegister
object OtherManager : RegisteredEvent {
    val file = JsonFile(
        filename = "other.json",
        type = OtherData::class.java,
        defaultFactory = { OtherData() },
        builder = { builder ->
            builder.registerTypeAdapter(Entry::class.java, object : JsonSerializer<Entry>, JsonDeserializer<Entry> {
                override fun serialize(src: Entry, typeOfSrc: java.lang.reflect.Type, context: JsonSerializationContext): JsonElement {
                    val jsonObject = context.serialize(src, src::class.java).asJsonObject
                    jsonObject.addProperty("type", src::class.simpleName)

                    return jsonObject
                }

                override fun deserialize(json: JsonElement, typeOfT: java.lang.reflect.Type, context: JsonDeserializationContext): Entry {
                    return when (val type = json.asJsonObject["type"].asString) {
                        "StringEntry" -> context.deserialize(json, StringEntry::class.java)
                        else -> throw JsonParseException("Unknown Entry type: $type")
                    }
                }
            })
            builder.create()
        }
    )

    val data
        get() = file.data

    override fun register() {
        registerJoinEvent {
            file.save()
        }

        registerShutdownEvent(1000) {
            file.save()
        }
    }

    fun getField(key : String) : Entry? {
        return data.savedStuff[key]
    }

    fun getField(key : String, defaultFactory : () -> Entry) : Entry {
        var result = data.savedStuff[key]
        if(result == null) {
            result = defaultFactory()
            setField(key, result)
        }

        return result
    }

    fun setField(key: String, entry: Entry) {
        data.savedStuff[key] = entry
    }
}