package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.RiccioFishingUtils
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.network.Network.authenticateUser
import cloud.glitchdev.rfu.utils.network.Network.getRequest
import cloud.glitchdev.rfu.utils.network.Network.isTokenExpired
import cloud.glitchdev.rfu.utils.network.Network.postRequest
import cloud.glitchdev.rfu.utils.network.Network.deleteRequest
import com.google.gson.Gson
import java.net.http.HttpRequest

object PartyHttp {
    private val gson = Gson()

    fun getExistingParties(callback: (List<FishingParty>) -> Unit) {
        getRequest("${RiccioFishingUtils.API_URL}/party") { response ->
            if(!response.isSuccessful()) {
                callback(mutableListOf())
                return@getRequest
            }

            try {
                val partiesArray = gson.fromJson(response.body, Array<FishingParty>::class.java)
                callback(partiesArray.toList())
            } catch (e: Exception) {
                e.printStackTrace()
                callback(mutableListOf())
            }
        }
    }

    fun createParty(party: FishingParty, callback : (Boolean) -> Unit) {
        if (isTokenExpired()) {
            authenticateUser()
            callback(false)
            return
        }

        postRequest("${RiccioFishingUtils.API_URL}/party", true, HttpRequest.BodyPublishers.ofString(party.toJson())) { response ->
            callback(response.isSuccessful())
        }
    }

    fun deleteParty(callback: (Boolean) -> Unit) {
        if (isTokenExpired()) {
            authenticateUser()
            callback(false)
            return
        }

        deleteRequest("${RiccioFishingUtils.API_URL}/party/${User.getUsername()}", true) { response ->
            callback(response.isSuccessful())
        }
    }
}