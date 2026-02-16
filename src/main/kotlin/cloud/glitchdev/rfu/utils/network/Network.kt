package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.RiccioFishingUtils
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.RiccioFishingUtils.API_URL
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.config.categories.BackendSettings
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Component
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.constants.text.TextEffects.*
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.google.gson.JsonParser
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
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
        registerJoinEvent {
            authenticateUser()
        }
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
            RFULogger.error("Error while sending GET request", e)
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
            RFULogger.error("Error while sending POST request", e)
            callback(Response(null))
        }
    }

    fun putRequest(url : String, useToken : Boolean = false, body: HttpRequest.BodyPublisher = HttpRequest.BodyPublishers.noBody(), callback: (Response) -> Unit) {
        try {
            val requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(body)
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
            RFULogger.error("Error while sending PUT request", e)
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
            RFULogger.error("Error while sending DELETE request", e)
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
            RFULogger.error("Error while checking token validity", e)
            return true
        }
    }

    /**
     * Authenticates through mojang's session server
     * and rfu back-end if the token has expired or is invalid
     */
    fun authenticateUser() {
        if (!BackendSettings.decisionMade) {
            sendAcknowledgementMessage()
            return
        }

        if (!BackendSettings.backendAccepted) {
            return
        }

        if(isTokenExpired()) {
            val session = mc.user
            val serverId = UUID.randomUUID().toString().replace("-", "")
            val sessionService = mc.services().sessionService

            try {
                sessionService.joinServer(
                    session.profileId,
                    session.accessToken,
                    serverId
                )

                postRequest("${API_URL}/auth/login?user=${User.getUsername()}&server=$serverId") { response ->
                    if(response.isSuccessful()) {
                        token = response.body
                        RFULogger.dev("Token Renewed")
                    }
                    else {
                        RFULogger.warn("Failed to log into RFU Back-end\n${response.body}")
                    }
                }

            } catch (e: Exception) {
                RFULogger.error("Verification failed: ${e.message}")
            }
        }
    }

    private fun sendAcknowledgementMessage() {
        val message = TextUtils.rfuLiteral("This mod utilizes a separate back-end for features like party finder. Do you want to enable it? ", TextStyle(YELLOW))

        val accept = Component.literal("$LIGHT_GREEN$BOLD[ACCEPT]")
            .withStyle { it.withClickEvent(ClickEvent.RunCommand("/rfubackend accept"))
                .withHoverEvent(HoverEvent.ShowText(Component.literal("${LIGHT_GREEN}Accept backend connection"))) }

        val deny = Component.literal(" $LIGHT_RED$BOLD[DENY]")
            .withStyle { it.withClickEvent(ClickEvent.RunCommand("/rfubackend deny"))
                .withHoverEvent(HoverEvent.ShowText(Component.literal("${LIGHT_RED}Deny backend connection"))) }

        message.append(accept).append(deny)
        Chat.sendMessage(message)
    }

    @Command
    object BackEndCommand : AbstractCommand("rfubackend") {
        override val description: String = "Used for accepting/denying back-end features upon first install."

        override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
            builder
                .then(literal("accept").executes {
                    BackendSettings.backendAccepted = true
                    BackendSettings.decisionMade = true
                    authenticateUser()
                    RiccioFishingUtils.saveConfig()
                    it.source.sendFeedback(TextUtils.rfuLiteral("Backend connection accepted. If you wish to disable it head to Backend Settings on /rfu", TextStyle(LIGHT_GREEN)))
                    return@executes 1
                })
                .then(literal("deny").executes {
                    BackendSettings.backendAccepted = false
                    BackendSettings.decisionMade = true
                    RiccioFishingUtils.saveConfig()
                    it.source.sendFeedback(TextUtils.rfuLiteral("Backend connection denied. If you change your mind head to Backend Settings on /rfu", TextStyle(LIGHT_RED)))
                    return@executes 1
                })
        }
    }

    @Command
    object TokenCommand : SimpleCommand("rfubackendtoken") {
        override val description: String = "Copies your RFU Back-end token to your clipboard."

        override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
            if(token != null) {
                mc.keyboardHandler.clipboard = token ?: "ERROR Blank Token"
                context.source.sendFeedback(TextUtils.rfuLiteral("Your rfu back-end token has been copied to your clipboard!",
                    TextStyle(LIGHT_GREEN)))
            }
            else {
                context.source.sendFeedback(TextUtils.rfuLiteral("You don't have a RFU back-end token!",
                    TextStyle(LIGHT_RED)))
            }
            return 1
        }
    }
}