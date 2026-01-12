package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.RiccioFishingUtils.minecraft
import cloud.glitchdev.rfu.RiccioFishingUtils.API_URL
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.Command
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.User
import com.google.gson.JsonParser
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.Base64
import java.util.UUID

@AutoRegister
object Network : RegisteredEvent {
    class Response(val response : HttpResponse<String>?) {
        val statusCode = response?.statusCode()
        val body = response?.body()

        fun isSuccessful() : Boolean {
            return statusCode != null && statusCode >= 200 && statusCode < 300
        }
    }

    private var token: String? = null
    private var expiresAt : Long? = null
    private val client = HttpClient.newHttpClient()

    override fun register() {
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, _ ->
            authenticateUser()
        }

        Command.registerCommand(
            literal("rfubackendtoken")
                .executes { context ->
                    minecraft.keyboard.clipboard = token
                    context.source.sendFeedback(TextUtils.rfuLiteral("Your rfu back-end token has been copied to your clipboard!",
                        TextStyle(TextColor.LIGHT_GREEN)))
                    return@executes 1
                }
        )
    }

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

    fun deleteRequest(url : String, useToken : Boolean = false, body: HttpRequest.BodyPublisher = HttpRequest.BodyPublishers.noBody(), callback: (Response) -> Unit) {
        try {
            val requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
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

    fun isTokenExpired(): Boolean {
        try {
            if(token == null) return true
            val parts = token!!.split(".")
            if (parts.size != 3) return true

            if(expiresAt == null) {
                val payload = parts[1]
                val decoder = Base64.getUrlDecoder()
                val decodedJson = String(decoder.decode(payload))

                val jsonObject = JsonParser.parseString(decodedJson).asJsonObject

                if (jsonObject.has("exp")) {
                    expiresAt = jsonObject["exp"].asLong
                }
                else {
                    return true
                }
            }

            val currentTimestamp = Instant.now().epochSecond
            return currentTimestamp > (expiresAt!! - 10)
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
    }

    /**
     * Authenticates through mojang's session server
     * and rfu back-end if the token has expired or is invalid
     */
    fun authenticateUser() {
        if(isTokenExpired()) {
            val session = minecraft.session
            val serverId = UUID.randomUUID().toString().replace("-", "")
            //? if <1.21.10 {
            /*val sessionService = minecraft.sessionService
            *///?} else {
            val sessionService = minecraft.apiServices.sessionService
            //?}

            try {
                sessionService.joinServer(
                    session.uuidOrNull,
                    session.accessToken,
                    serverId
                )

                postRequest("${API_URL}/auth/login?user=${User.getUsername()}&server=$serverId") { response ->
                    if(response.isSuccessful()) {
                        token = response.body
                        println("Token Renewed")
                    }
                }

            } catch (e: Exception) {
                System.err.println("Verification failed: ${e.message}")
            }
        }
    }
}