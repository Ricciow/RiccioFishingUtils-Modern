package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.Companion.API_URL
import cloud.glitchdev.rfu.model.party.FishingParty
import com.google.gson.Gson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object Network {
    class Response(val response : HttpResponse<String>?) {
        val statusCode = response?.statusCode()
        val body = response?.body()

        fun isSuccessful() : Boolean {
            return statusCode != null && statusCode >= 200 && statusCode < 300
        }
    }

    private val client = HttpClient.newHttpClient()
    private val gson = Gson()

    fun getRequest(url : String, callback: (Response) -> Unit) {
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
            callback(Response(null))
        }
    }

    fun postRequest(url : String, callback: (Response) -> Unit) {
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody()) // Assuming empty body for now
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
}