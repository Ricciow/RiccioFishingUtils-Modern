package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.PartyEvents
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.runPartyCreatedTasks
import cloud.glitchdev.rfu.model.network.WebSocketEvent
import cloud.glitchdev.rfu.model.network.WebSocketEventType
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.model.party.JoinPartyNotification
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.Coroutines
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.ErrorEvents.registerErrorMessageEvent
import cloud.glitchdev.rfu.events.managers.WebSocketEvents.registerConnectionStatusChangedEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Clock

@AutoRegister
object PartyWebSocket : RegisteredEvent {
    private val gson = Gson()
    private var connectionLostJob: Job? = null
    
    var myParty: FishingParty? = null
        private set(value) {
            field = value
            PartyEvents.MyPartyChanged.runTasks(value)
        }

    private var lastJoinTarget: String? = null

    override fun register() {
        RFULogger.dev("Registering PartyWebSocket")

        registerConnectionStatusChangedEvent { connected ->
            if (connected) {
                connectionLostJob?.cancel()
                connectionLostJob = null
            } else if (myParty != null) {
                connectionLostJob = Coroutines.launch {
                    val lastTime = WebSocketClient.lastIncomingTime ?: Clock.System.now()
                    val elapsed = Clock.System.now() - lastTime
                    val remaining = 60000 - elapsed.inWholeMilliseconds
                    
                    if (remaining > 0) {
                        delay(remaining)
                    }
                    
                    if (!WebSocketClient.isConnected && myParty != null) {
                        Chat.sendMessage(TextUtils.rfupfLiteral("Party dequeued (Connection lost)", TextColor.LIGHT_RED))
                        myParty = null
                    }
                }
            }
        }

        registerErrorMessageEvent { message, origin ->
            if (message == "Target user is not currently connected to the WebSocket.") {
                lastJoinTarget?.let { target ->
                    Party.requestEntry(target)
                    lastJoinTarget = null
                }
            }
        }

        val listCallback: (String) -> Unit = { msg ->
            try {
                val type = object : TypeToken<WebSocketEvent<List<FishingParty>>>() {}.type
                val event = gson.fromJson<WebSocketEvent<List<FishingParty>>>(msg, type)
                
                if (event.type == WebSocketEventType.SYNC) {
                    val newParties = event.data ?: emptyList()
                    myParty = newParties.find { it.user == User.getUsername() }
                    PartyEvents.handleSync(newParties)
                }
            } catch (e: Exception) {
                RFULogger.error("Error parsing party list sync: ", e)
            }
        }

        val updateCallback: (String) -> Unit = { msg ->
            try {
                val type = object : TypeToken<WebSocketEvent<FishingParty>>() {}.type
                val event = gson.fromJson<WebSocketEvent<FishingParty>>(msg, type)
                
                when (event.type) {
                    WebSocketEventType.CREATED, WebSocketEventType.UPDATED -> {
                        event.data?.let { updatedParty ->
                            if (updatedParty.user == User.getUsername()) {
                                myParty = updatedParty
                                if (event.type == WebSocketEventType.CREATED) {
                                    runPartyCreatedTasks(updatedParty)
                                }
                            }
                            PartyEvents.handleUpdate(updatedParty)
                        }
                    }
                    WebSocketEventType.DELETED -> {
                        event.id?.let { user ->
                            if (user == User.getUsername()) {
                                if (myParty != null) {
                                    Chat.sendMessage(TextUtils.rfupfLiteral("Party dequeued", TextColor.LIGHT_RED))
                                }
                                myParty = null
                            }
                            PartyEvents.handleDelete(user)
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                RFULogger.error("Error parsing party update: ", e)
            }
        }

        val joinRequestCallback: (String) -> Unit = { msg ->
            try {
                val notification = gson.fromJson(msg, JoinPartyNotification::class.java)
                PartyEvents.JoinRequest.runTasks(notification.applicant)
            } catch (e: Exception) {
                RFULogger.error("Error parsing join request notification: ", e)
            }
        }

        WebSocketClient.subscribe("/topic/parties", updateCallback)
        WebSocketClient.subscribe("/app/topic/parties", listCallback)
        WebSocketClient.subscribe("/user/queue/parties", listCallback)
        WebSocketClient.subscribe("/user/queue/join-requests", joinRequestCallback)
    }

    fun syncParties() {
        WebSocketClient.send("/app/party/sync", "")
    }

    fun publishParty(party: FishingParty) {
        WebSocketClient.send("/app/party/publish", party)
    }

    fun editParty(party: FishingParty) {
        WebSocketClient.send("/app/party/edit", party)
    }

    fun submitParty(party: FishingParty) {
        if (myParty == null) {
            publishParty(party)
        } else {
            editParty(party)
        }
    }

    fun deleteParty(user: String) {
        WebSocketClient.send("/app/party/delete", gson.toJson(user))
        if (user == User.getUsername()) {
            if (myParty != null) {
                Chat.sendMessage(TextUtils.rfupfLiteral("Party dequeued", TextColor.LIGHT_RED))
            }
            myParty = null
        }
    }

    fun joinParty(targetUser: String) {
        lastJoinTarget = targetUser
        Party.requestedUser = targetUser
        WebSocketClient.send("/app/party/join", mapOf("targetUser" to targetUser))
        Chat.sendMessage(TextUtils.rfupfLiteral("Sent a join request to $targetUser", TextColor.YELLOW))
    }

    fun reportParty(user: String) {
        WebSocketClient.send("/app/party/report", gson.toJson(user))
    }
}
