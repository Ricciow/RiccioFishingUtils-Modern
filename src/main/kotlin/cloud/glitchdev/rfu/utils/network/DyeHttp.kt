package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.RiccioFishingUtils.API_URL
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.BackendSettings
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.model.dye.Dyes
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.network.Network.authenticateUser
import cloud.glitchdev.rfu.utils.network.Network.getRequest
import cloud.glitchdev.rfu.utils.network.Network.isTokenExpired
import cloud.glitchdev.rfu.utils.network.Network.postRequest
import com.google.gson.Gson
import gg.essential.universal.utils.toUnformattedString
import java.net.http.HttpRequest

@AutoRegister
object DyeHttp : RegisteredEvent {
    private val gson = Gson()
    var currentDyes : Dyes? = null
        private set
    private val dyeIndexes : Set<Int> = setOf(29, 31, 33)
    private var isRequesting = false

    override fun register() {
        getCurrentDyes {}

        //Renew current dyes if needed
        registerTickEvent(0, 300000) {
            if(areDyesOutdated()) {
                getCurrentDyes()
            }
        }

        registerContainerOpenEvent { _, itens ->
            if(!BackendSettings.shareDyeData) return@registerContainerOpenEvent
            if(!areDyesOutdated()) return@registerContainerOpenEvent
            if(isRequesting) return@registerContainerOpenEvent

            if(mc.screen?.title?.string == "Dyes") {
                val dyes = dyeIndexes.map { itens[it] }

                if(dyes.any { it.customName == null }) return@registerContainerOpenEvent

                val dyeNames = dyes.map { it.customName?.toUnformattedString() }

                val newDyes = Dyes(
                    dyeNames[0],
                    dyeNames[1],
                    dyeNames[2]
                )

                isRequesting = true
                createCurrentDyes(newDyes) {
                    isRequesting = false
                }
            }
        }
    }

    private fun areDyesOutdated() : Boolean {
        return currentDyes == null || currentDyes?.isOutdated() ?: true
    }

    fun getCurrentDyes(callback: (Dyes?) -> Unit = {}) {
        RFULogger.dev("Fetching new dye data.")
        getRequest("${API_URL}/dye") { response ->
            if(!response.isSuccessful()) {
                callback(null)
                return@getRequest
            }

            try {
                val dyes = gson.fromJson(response.body, Dyes::class.java)
                currentDyes = dyes
                callback(dyes)
            } catch (e: Exception) {
                RFULogger.error("Error while getting current dyes: ", e)
                currentDyes = null
                callback(null)
            }
        }
    }

    fun createCurrentDyes(dyes : Dyes, callback: (Dyes?) -> Unit = {}) {
        RFULogger.dev("Uploading new dye data")
        if (isTokenExpired()) {
            authenticateUser()
            callback(null)
            return
        }

        postRequest("${API_URL}/dye", true, HttpRequest.BodyPublishers.ofString(dyes.toJson())) { response ->
            if(!response.isSuccessful()) {
                callback(null)
                return@postRequest
            }

            try {
                val dyes = gson.fromJson(response.body, Dyes::class.java)

                currentDyes = dyes
                callback(dyes)
            } catch (e: Exception) {
                RFULogger.error("Error while updating current dyes: ", e)
                currentDyes = null
                callback(null)
            }
        }
    }
}