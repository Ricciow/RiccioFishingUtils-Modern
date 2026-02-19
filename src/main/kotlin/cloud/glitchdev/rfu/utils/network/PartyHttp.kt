package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.RiccioFishingUtils.API_URL
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.network.Network.authenticateUser
import cloud.glitchdev.rfu.utils.network.Network.getRequest
import cloud.glitchdev.rfu.utils.network.Network.isTokenExpired
import cloud.glitchdev.rfu.utils.network.Network.postRequest
import cloud.glitchdev.rfu.utils.network.Network.putRequest
import cloud.glitchdev.rfu.utils.network.Network.deleteRequest
import com.google.gson.Gson
import java.net.http.HttpRequest

object PartyHttp {
    private val gson = Gson()
    var currentParty : FishingParty? = null
    var parties : List<FishingParty> = mutableListOf()
        private set

    fun getParties(callback: (List<FishingParty>?) -> Unit = {}) {
        getRequest("${API_URL}/party") { response ->
            if(!response.isSuccessful()) {
                mc.execute { callback(null) }
                return@getRequest
            }

            try {
                val partiesArray = gson.fromJson(response.body, Array<FishingParty>::class.java)
                parties = partiesArray.toList()
                mc.execute { callback(parties) }
            } catch (e: Exception) {
                e.printStackTrace()
                mc.execute { callback(null) }
            }
        }
    }

    fun createParty(party: FishingParty, callback : (Boolean) -> Unit) {
        if (isTokenExpired()) {
            authenticateUser()
            mc.execute { callback(false) }
            return
        }

        postRequest("${API_URL}/party", true, HttpRequest.BodyPublishers.ofString(party.toJson())) { response ->
            if(response.isSuccessful()) {
                currentParty = party
            }
            mc.execute { callback(response.isSuccessful()) }
        }
    }

    fun updateParty(party : FishingParty, callback: (Boolean) -> Unit) {
        if (isTokenExpired()) {
            authenticateUser()
            mc.execute { callback(false) }
            return
        }

        putRequest("${API_URL}/party", true, HttpRequest.BodyPublishers.ofString(party.toJson())) { response ->
            if(response.isSuccessful()) {
                currentParty = party
            }
            mc.execute { callback(response.isSuccessful()) }
        }
    }

    fun deleteParty(callback: (Boolean) -> Unit) {
        if (isTokenExpired()) {
            authenticateUser()
            mc.execute { callback(false) }
            return
        }

        deleteRequest("${API_URL}/party/${User.getUsername()}", true) { response ->
            if(response.isSuccessful()) {
                currentParty = null
            }
            mc.execute { callback(response.isSuccessful()) }
        }
    }
}