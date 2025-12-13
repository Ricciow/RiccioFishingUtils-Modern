package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.Companion.API_URL
import cloud.glitchdev.rfu.RiccioFishingUtils.Companion.minecraft
import cloud.glitchdev.rfu.model.party.FishingParty
import com.google.gson.Gson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

object Network {
    class Response(val response : HttpResponse<String>?) {
        val statusCode = response?.statusCode()
        val body = response?.body()

        fun isSuccessful() : Boolean {
            return statusCode != null && statusCode >= 200 && statusCode < 300
        }
    }

    private var token: String? = null
    private val client = HttpClient.newHttpClient()
    private val gson = Gson()

    fun getRequest(url : String, useToken : Boolean = false, callback: (Response) -> Unit) {
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build()

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle { res, ex ->
                    if (ex != null) {
                        callback(Response(null))
                    } else {
                        callback(Response(res))
                    }
                }
        }
        catch (e : Exception) {
            e.printStackTrace()
            callback(Response(null))
        }
    }

    fun postRequest(url : String, useToken : Boolean = false, body: HttpRequest.BodyPublisher = HttpRequest.BodyPublishers.noBody(), callback: (Response) -> Unit) {
        try {
            val requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(body)
                .header("Content-Type", "Application/Json")

            if(useToken) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle { res, ex ->
                    if (ex != null) {
                        callback(Response(null))
                    } else {
                        callback(Response(res))
                    }
                }
        }
        catch (e : Exception) {
            e.printStackTrace()
            callback(Response(null))
        }
    }

    fun getExistingParties(callback: (List<FishingParty>) -> Unit) {
        getRequest("$API_URL/party") { response ->
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
        if (token == null) {
            authenticateUser()
            callback(false)
            return
        }

        postRequest("$API_URL/party", true, HttpRequest.BodyPublishers.ofString(party.toJson())) { response ->
            callback(response.isSuccessful())
        }
    }

    /**
     * Authenticates through mojang's session server
     * and rfu back-end
     */
    fun authenticateUser() {
        val session = minecraft.session
        val serverId = UUID.randomUUID().toString().replace("-", "")
        //? if <1.21.10 {
        /*val sessionService = minecraft.sessionService
        *///?} else  {
        val sessionService = minecraft.apiServices.sessionService
        //?}

        try {
            sessionService.joinServer(
                session.uuidOrNull,
                session.accessToken,
                serverId
            )

            postRequest("$API_URL/auth/login?user=${User.getUsername()}&server=$serverId") { response ->
                if(response.isSuccessful()) {
                    token = response.body
                }
            }

        } catch (e: Exception) {
            System.err.println("Verification failed: ${e.message}")
        }
    }
}