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

    fun getRequest(url : String) : Response {
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            return Response(response)
        }
        catch (e : Exception) {
            return Response(null)
        }
    }

    fun postRequest(url : String) : Response {
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            return Response(response)
        }
        catch (e : Exception) {
            return Response(null)
        }
    }

    fun getExistingParties() : List<FishingParty> {
        val response = getRequest("$API_URL/party")

        if(!response.isSuccessful()) return mutableListOf()

        val partiesArray = gson.fromJson(response.body, Array<FishingParty>::class.java)

        return partiesArray.toList()
    }
}