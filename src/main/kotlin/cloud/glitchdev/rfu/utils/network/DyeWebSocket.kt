package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.BackendSettings
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.DyeEvents
import cloud.glitchdev.rfu.model.dye.Dyes
import cloud.glitchdev.rfu.model.network.WebSocketEvent
import cloud.glitchdev.rfu.model.network.WebSocketEventType
import cloud.glitchdev.rfu.utils.RFULogger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import gg.essential.universal.utils.toUnformattedString

@AutoRegister
object DyeWebSocket : RegisteredEvent {
    private val gson = Gson()
    var currentDyes : Dyes? = null
        private set
    private val dyeIndexes : Set<Int> = setOf(29, 31, 33)

    override fun register() {
        RFULogger.dev("Registering DyeWebSocket")
        val callback : (String) -> Unit = { msg ->
            try {
                val type = object : TypeToken<WebSocketEvent<Dyes>>() {}.type
                val event = gson.fromJson<WebSocketEvent<Dyes>>(msg, type)

                when (event.type) {
                    WebSocketEventType.SYNC, WebSocketEventType.CREATED, WebSocketEventType.UPDATED -> {
                        currentDyes = event.data
                        RFULogger.dev("Dyes updated via WebSocket: ${currentDyes?.dye1}, ${currentDyes?.dye2}, ${currentDyes?.dye3}")
                        DyeEvents.trigger(currentDyes)
                    }
                    WebSocketEventType.DELETED -> {
                        currentDyes = null
                        RFULogger.dev("Dyes deleted via WebSocket")
                        DyeEvents.trigger(null)
                    }
                }
            } catch (e: Exception) {
                RFULogger.error("Error while parsing dye websocket message: ", e)
            }
        }

        RFULogger.dev("Subscribing to dye topics...")
        WebSocketClient.subscribe("/topic/dyes", callback)
        WebSocketClient.subscribe("/app/topic/dyes", callback)

        registerContainerOpenEvent { _, itens ->
            if(!BackendSettings.shareDyeData) return@registerContainerOpenEvent
            if(!areDyesOutdated()) return@registerContainerOpenEvent

            if(mc.screen?.title?.string == "Dyes") {
                val dyes = dyeIndexes.map { itens[it] }

                if(dyes.any { it.customName == null }) return@registerContainerOpenEvent

                val dyeNames = dyes.map { it.customName?.toUnformattedString() }

                val newDyes = Dyes(
                    dyeNames[0],
                    dyeNames[1],
                    dyeNames[2]
                )

                updateDyes(newDyes)
            }
        }
    }

    private fun areDyesOutdated() : Boolean {
        return currentDyes == null || currentDyes?.isOutdated() ?: true
    }

    fun updateDyes(dyes : Dyes) {
        RFULogger.dev("Uploading new dye data via WebSocket")
        WebSocketClient.send("/app/dye/update", dyes.toJson())
    }
}
