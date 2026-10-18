package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.RiccioFishingUtils.WS_URL
import cloud.glitchdev.rfu.RiccioFishingUtils.RFU_VERSION
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.events.managers.ErrorEvents
import cloud.glitchdev.rfu.events.managers.AfkEvents
import cloud.glitchdev.rfu.events.managers.WebSocketEvents
import cloud.glitchdev.rfu.utils.Coroutines
import com.google.gson.Gson
import java.net.URI
import java.net.ConnectException
import java.net.UnknownHostException
import java.net.http.HttpClient
import java.net.http.HttpTimeoutException
import java.net.http.WebSocket
import java.net.http.WebSocketHandshakeException
import java.nio.channels.UnresolvedAddressException
import javax.net.ssl.SSLException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

object WebSocketClient {
    private val client = HttpClient.newHttpClient()
    private val USER_AGENT = "Java-http-client/${System.getProperty("java.version")} rfu:${RFU_VERSION.friendlyString}"
    private var webSocket: WebSocket? = null
    private val subscriptions = ConcurrentHashMap<String, (String) -> Unit>()
    private val gson = Gson()
    private var lastAuthToken: String? = null
    private var reconnectAttempts = 0
    @Volatile
    private var isReconnecting = false
    @Volatile
    private var isConnecting = false
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    @Volatile
    private var nextReconnectAt = 0L
    @Volatile
    private var disconnectReason: String? = null
    @Volatile
    var lastConnectionIssue: String? = null
        private set

    val connectionStatus: String
        get() {
            val authenticationStatus = Network.authenticationStatus
            return when {
                isConnected -> "${TextColor.LIGHT_GREEN}Connected."
                authenticationStatus != null -> authenticationStatus
                isConnecting -> "${TextColor.YELLOW}Connecting to the RFU back-end..."
                isReconnecting -> {
                    val seconds = ((nextReconnectAt - System.currentTimeMillis() + 999) / 1000).coerceAtLeast(0)
                    "${TextColor.LIGHT_RED}Retrying in ${seconds}s (attempt $reconnectAttempts)."
                }
                disconnectReason == "AFK" && AfkEvents.isAfk -> "${TextColor.LIGHT_RED}Connection paused while AFK. Move to auto-reconnect."
                else -> "${TextColor.LIGHT_RED}No active connection."
            }
        }
    
    @Volatile
    var isConnected = false
        private set(value) {
            if (field != value) {
                field = value
                WebSocketEvents.trigger(value)
            }
        }

    fun connect(authToken: String) {
        lastAuthToken = authToken
        if (isConnected || isReconnecting || isConnecting) return
        
        isConnecting = true
        disconnectReason = null
        val wsUrl = WS_URL
        RFULogger.dev("Connecting to WebSocket: $wsUrl")
        
        client.newWebSocketBuilder()
            .header("User-Agent", USER_AGENT)
            .buildAsync(URI.create(wsUrl), object : WebSocket.Listener {
                override fun onOpen(webSocket: WebSocket) {
                    RFULogger.dev("WebSocket Opened (URL: $wsUrl), sending CONNECT frame...")
                    isReconnecting = false
                    val tokenWithBearer = if (authToken.startsWith("Bearer ")) authToken else "Bearer $authToken"
                    sendFrame(webSocket, "CONNECT", mapOf(
                        "accept-version" to "1.1,1.2",
                        "heart-beat" to "10000,10000",
                        "Authorization" to tokenWithBearer
                    ))
                    webSocket.request(1)
                }

                override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
                    val frame = data.toString()
                    if (frame.trim() != "") {
                        handleFrame(frame)
                    }
                    webSocket.request(1)
                    return null
                }

                override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String): CompletionStage<*>? {
                    RFULogger.info("WebSocket Closed: $statusCode $reason")
                    if (lastAuthToken != null && lastConnectionIssue == null) {
                        lastConnectionIssue = "The server closed the connection (code $statusCode)."
                    }
                    isConnected = false
                    isConnecting = false
                    if (statusCode != WebSocket.NORMAL_CLOSURE) {
                        attemptReconnect()
                    }
                    return null
                }

                override fun onError(webSocket: WebSocket, error: Throwable) {
                    RFULogger.error("WebSocket Error (on Listener): ", error)
                    lastConnectionIssue = describeConnectionError(error)
                    isConnected = false
                    isConnecting = false
                    attemptReconnect()
                }
            }).thenAccept { ws ->
                RFULogger.dev("WebSocket buildAsync successful")
                webSocket = ws
            }.exceptionally { error ->
                RFULogger.error("WebSocket buildAsync failed: ", error)
                lastConnectionIssue = describeConnectionError(error)
                isReconnecting = false
                isConnecting = false
                attemptReconnect()
                null
            }
    }

    private fun describeConnectionError(error: Throwable): String {
        val causes = generateSequence(error) { it.cause }.toList()
        val handshake = causes.filterIsInstance<WebSocketHandshakeException>().firstOrNull()
        return when {
            causes.any { it is UnknownHostException || it is UnresolvedAddressException } -> "Could not resolve the RFU server address (DNS)."
            causes.any { it is HttpTimeoutException } -> "The connection to RFU timed out."
            causes.any { it is ConnectException } -> "Could not reach the RFU server."
            causes.any { it is SSLException } -> "Could not establish a secure connection to RFU (TLS)."
            handshake != null -> "The server rejected the connection (HTTP ${handshake.response.statusCode()})."
            else -> "Connection failed (${causes.last().javaClass.simpleName}). Check latest.log for details."
        }
    }

    private fun attemptReconnect() {
        if (isConnected || isReconnecting || lastAuthToken == null) return
        isReconnecting = true
        
        reconnectAttempts++
        val baseDelay = minOf(2.0.pow(reconnectAttempts), 60.0).toLong()
        val jitter = (Math.random() * 5).toLong()
        val delaySeconds = baseDelay + jitter
        nextReconnectAt = System.currentTimeMillis() + delaySeconds * 1000
        RFULogger.info("Attempting to reconnect WebSocket in $delaySeconds seconds (attempt $reconnectAttempts)...")
        
        reconnectJob = Coroutines.launch {
            delay(delaySeconds * 1000)
            isReconnecting = false
            nextReconnectAt = 0L
            
            if (Network.isTokenExpired()) {
                RFULogger.dev("Token expired during reconnect, re-authenticating...")
                Network.authenticateUser()
            } else {
                lastAuthToken?.let { connect(it) }
            }
        }
    }

    private fun handleFrame(frameData: String) {
        val lines = frameData.split("\n")
        if (lines.isEmpty() || lines[0].isBlank()) return
        
        val command = lines[0].trim()
        val headers = mutableMapOf<String, String>()
        var bodyIndex = -1
        
        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) {
                bodyIndex = i + 1
                break
            }
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                headers[parts[0]] = parts[1]
            }
        }
        
        val body = if (bodyIndex != -1 && bodyIndex < lines.size) {
            lines.subList(bodyIndex, lines.size).joinToString("\n").trimEnd { it == '\u0000' }
        } else ""

        RFULogger.dev("STOMP:\n--- Incoming STOMP Frame ---\n" +
                "Command: $command\n" +
                "Headers: $headers\n" +
                "Body: ${if (body.isEmpty()) "(empty)" else body}\n" +
                "---------------------------")

        if (command == "CONNECTED") {
            lastConnectionIssue = null
            reconnectAttempts = 0
            isConnecting = false
            isConnected = true
            RFULogger.info("STOMP Connected")

            heartbeatJob?.cancel()
            heartbeatJob = Coroutines.launch {
                while (isActive && isConnected) {
                    delay(30000)
                    try {
                        webSocket?.sendText("\n", true)
                    } catch (e: Exception) {
                        RFULogger.error("Failed to send heartbeat: ", e)
                    }
                }
            }

            subscribe("/user/queue/errors") { msg ->
                try {
                    val map = gson.fromJson(msg, Map::class.java)
                    val errorMsg = map["message"]?.toString() ?: "Unknown error"
                    val origin = map["origin"]?.toString() ?: "UNKNOWN"
                    ErrorEvents.trigger(errorMsg, origin)
                } catch (e: Exception) {
                    RFULogger.error("Error parsing backend error message: ", e)
                }
            }

            subscriptions.forEach { (topic, _) ->
                RFULogger.dev("Re-subscribing to $topic after reconnection")
                subscribe(topic)
            }
        } else if (command == "MESSAGE") {
            val destination = headers["destination"]
            if (destination != null) {
                if (subscriptions.containsKey(destination)) {
                    subscriptions[destination]?.invoke(body)
                } else {
                    RFULogger.dev("No subscription found for destination: $destination")
                }
            }
        } else if (command == "ERROR") {
            lastConnectionIssue = "The RFU back-end rejected the session. Try /rfubackend reconnect."
            isConnecting = false
            RFULogger.error("Received STOMP ERROR frame:\nHeaders: $headers\nBody: $body")
        } else {
            RFULogger.dev("Unhandled STOMP command: $command")
        }
    }

    fun subscribe(topic: String, callback: ((String) -> Unit)? = null) {
        if (callback != null) {
            subscriptions[topic] = callback
        }
        
        if (isConnected) {
            sendFrame(webSocket!!, "SUBSCRIBE", mapOf(
                "id" to "sub-${topic.hashCode()}",
                "destination" to topic
            ))
        } else {
            RFULogger.dev("Queued subscription for $topic (not connected yet)")
        }
    }

    fun send(destination: String, payload: Any) {
        if (isConnected) {
            val body = if (payload is String) payload else gson.toJson(payload)
            sendFrame(webSocket!!, "SEND", mapOf(
                "destination" to destination,
                "content-type" to "application/json"
            ), body)
        }
    }

    private fun sendFrame(ws: WebSocket, command: String, headers: Map<String, String>, body: String = "") {
        val frame = StringBuilder()
        frame.append(command).append("\r\n")
        headers.forEach { (k, v) ->
            frame.append(k).append(":").append(v).append("\r\n")
        }
        frame.append("\r\n")
        frame.append(body)
        frame.append("\u0000")
        
        RFULogger.dev("STOMP:\n---- Outgoing STOMP Frame ---\n" +
                "Command: $command\n" +
                "Headers: $headers\n" +
                "Body: ${if (body.isEmpty()) "(empty)" else body}\n" +
                "---------------------------")
        
        try {
            ws.sendText(frame.toString(), true)
        } catch (e: Exception) {
            RFULogger.error("Error sending STOMP frame: ", e)
        }
    }

    fun disconnect(reason: String = "Disconnecting") {
        if(webSocket != null) RFULogger.info("WebSocket Disconnecting: $reason")
        disconnectReason = reason
        lastConnectionIssue = null
        reconnectJob?.cancel()
        reconnectJob = null
        nextReconnectAt = 0L
        lastAuthToken = null
        heartbeatJob?.cancel()
        heartbeatJob = null
        webSocket?.sendClose(WebSocket.NORMAL_CLOSURE, reason)
        webSocket = null
        isConnected = false
        isReconnecting = false
        isConnecting = false
    }
}
