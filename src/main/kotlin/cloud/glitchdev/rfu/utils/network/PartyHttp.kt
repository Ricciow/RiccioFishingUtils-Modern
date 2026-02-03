package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.RiccioFishingUtils.API_URL
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

    fun getExistingParties(callback: (Pair<Boolean, List<FishingParty>>) -> Unit) {
        getRequest("${API_URL}/party") { response ->
            if(!response.isSuccessful()) {
                callback(Pair(false, mutableListOf()))
                return@getRequest
            }

            try {
                val partiesArray = gson.fromJson(response.body, Array<FishingParty>::class.java)
                callback(Pair(true, partiesArray.toList()))
            } catch (e: Exception) {
                e.printStackTrace()
                callback(Pair(false, mutableListOf()))
            }
        }
    }

    fun createParty(party: FishingParty, callback : (Boolean) -> Unit) {
        if (isTokenExpired()) {
            authenticateUser()
            callback(false)
            return
        }

        postRequest("${API_URL}/party", true, HttpRequest.BodyPublishers.ofString(party.toJson())) { response ->
            callback(response.isSuccessful())
            if(response.isSuccessful()) {
                currentParty = party
            }
        }
    }

    fun updateParty(party : FishingParty, callback: (Boolean) -> Unit) {
        if (isTokenExpired()) {
            authenticateUser()
            callback(false)
            return
        }

        putRequest("${API_URL}/party", true, HttpRequest.BodyPublishers.ofString(party.toJson())) { response ->
            callback(response.isSuccessful())
            if(response.isSuccessful()) {
                currentParty = party
            }
        }
    }

    fun deleteParty(callback: (Boolean) -> Unit) {
        if (isTokenExpired()) {
            authenticateUser()
            callback(false)
            return
        }

        deleteRequest("${API_URL}/party/${User.getUsername()}", true) { response ->
            callback(response.isSuccessful())
            if(response.isSuccessful()) {
                currentParty = null
            }
        }
    }
}