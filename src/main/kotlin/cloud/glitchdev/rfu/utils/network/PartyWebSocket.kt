package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.config.categories.BackendSettings
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.chat.RegexConstants.PLAYER_REGEX
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents
import cloud.glitchdev.rfu.model.network.WebSocketEvent
import cloud.glitchdev.rfu.model.network.WebSocketEventType
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.model.party.JoinPartyNotification
import cloud.glitchdev.rfu.model.party.JoinPartyRequest
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.Coroutines
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAllowGameEvent
import cloud.glitchdev.rfu.events.managers.ErrorEvents.registerErrorMessageEvent
import cloud.glitchdev.rfu.events.managers.WebSocketEvents.registerConnectionStatusChangedEvent
import cloud.glitchdev.rfu.gui.components.partyfinder.UIPartyPresetsModal
import cloud.glitchdev.rfu.utils.dsl.isIgnored
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

import cloud.glitchdev.rfu.model.party.PlayerRequisitesRequest
import cloud.glitchdev.rfu.model.party.PlayerRequisitesResult
import cloud.glitchdev.rfu.party.PartyRequirementsManager
import kotlin.time.Duration.Companion.hours

@AutoRegister
object PartyWebSocket : RegisteredEvent {
    private val gson = Gson()
    private var connectionLostJob: Job? = null
    private var lastSubmitTime = 0L

    var lastParty: FishingParty? = null
        private set

    var myParty: FishingParty? = null
        private set(value) {
            field = value
            if (value != null) {
                lastParty = value.deepCopy()
            }
            PartyFinderEvents.MyPartyChanged.runTasks(value)
        }

    private var lastJoinTarget: String? = null
    private var lastJoinTime: Instant? = null
    private var pendingJoinJob: Job? = null
    private var pendingJoinHasError = false

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
                        myParty?.let { lastParty = it.deepCopy() }
                        sendPartyDequeuedMessage("Connection lost")
                        myParty = null
                    }
                }
            }
        }

        registerErrorMessageEvent { message, origin ->
            if (origin == "/app/party/join" || origin.endsWith("/party/join")) {
                pendingJoinHasError = true
                pendingJoinJob?.cancel()
            }
            if (message == "Target user is not currently connected to the WebSocket.") {
                lastJoinTarget?.let { target ->
                    Party.requestEntry(target)
                    lastJoinTarget = null
                }
            }
        }

        registerAllowGameEvent("""-----------------------------------------------------\n($PLAYER_REGEX) has invited you to join their party!\nYou have 60 seconds to accept\. Click here to join!\n-----------------------------------------------------""".toExactRegex()) { _, _, matches ->
            val inviter = matches?.groupValues?.getOrNull(1)?.removeRankTag() ?: return@registerAllowGameEvent true
            if (inviter.isIgnored()) return@registerAllowGameEvent false
            val now = Clock.System.now()

            if (inviter == lastJoinTarget && now - lastJoinTime!! < 30.seconds) {
                Chat.sendCommand("p join $inviter")
                lastJoinTarget = null
                return@registerAllowGameEvent false
            }

            true
        }

        val listCallback: (String) -> Unit = { msg ->
            try {
                val type = object : TypeToken<WebSocketEvent<List<FishingParty>>>() {}.type
                val event = gson.fromJson<WebSocketEvent<List<FishingParty>>>(msg, type)
                
                if (event.type == WebSocketEventType.SYNC) {
                    val newParties = event.data ?: emptyList()
                    myParty = newParties.find { it.user == User.getUsername() }
                    PartyFinderEvents.handleSync(newParties)
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
                                    PartyFinderEvents.handleCreated(updatedParty)
                                }
                            }
                            PartyFinderEvents.handleUpdate(updatedParty)
                        }
                    }
                    WebSocketEventType.DELETED -> {
                        event.id?.let { user ->
                            if (user == User.getUsername()) {
                                if (myParty != null) {
                                    myParty?.let { lastParty = it.deepCopy() }
                                    sendPartyDequeuedMessage()
                                }
                                myParty = null
                            }
                            PartyFinderEvents.handleDelete(user)
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
                PartyFinderEvents.JoinRequest.runTasks(notification.applicant)
            } catch (e: Exception) {
                RFULogger.error("Error parsing join request notification: ", e)
            }
        }

        val requisitesCallback: (String) -> Unit = { msg ->
            try {
                val type = object : TypeToken<WebSocketEvent<PlayerRequisitesResult>>() {}.type
                val event = gson.fromJson<WebSocketEvent<PlayerRequisitesResult>>(msg, type)
                if (event.type == WebSocketEventType.SYNC && event.data != null) {
                    PartyRequirementsManager.updatePlayerRequisites(event.data, User.profileId)
                }
            } catch (e: Exception) {
                RFULogger.error("Error parsing player requisites sync: ", e)
            }
        }

        WebSocketClient.subscribe("/topic/parties", updateCallback)
        WebSocketClient.subscribe("/app/topic/parties", listCallback)
        WebSocketClient.subscribe("/user/queue/parties", listCallback)
        WebSocketClient.subscribe("/user/queue/join-requests", joinRequestCallback)
        WebSocketClient.subscribe("/user/queue/party/requisites", requisitesCallback)
    }

    fun requestPlayerRequisites(profileId: String? = User.profileId, force: Boolean = false) {
        if (!force) {
            val cachedProfile = PartyRequirementsManager.cachedProfileId
            val lastFetch = PartyRequirementsManager.lastRequisitesFetchTime
            if (profileId == cachedProfile && lastFetch != null && (Clock.System.now() - lastFetch) < 1.hours) {
                return
            }
        }
        if (profileId != PartyRequirementsManager.cachedProfileId) {
            PartyRequirementsManager.clearPlayerRequisites()
        }
        WebSocketClient.send("/app/party/requisites", PlayerRequisitesRequest(profileId))
    }

    fun syncParties() {
        WebSocketClient.send("/app/party/sync", "")
    }

    fun sendPartyDequeuedMessage(reason: String? = null) {
        val text = if (reason != null) "Party dequeued ($reason)" else "Party dequeued"
        val message = TextUtils.rfupfLiteral("$text ", TextColor.LIGHT_RED)

        val requeueButton = Component.literal("${TextColor.LIGHT_GREEN}${TextEffects.BOLD}[Requeue]")
            .setStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand("/rfurequeue"))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("${TextColor.YELLOW}Click to requeue your party!")))
            )

        message.append(requeueButton)
        Chat.sendMessage(message)
    }

    fun publishParty(party: FishingParty) {
        party.profileId = User.profileId
        lastParty = party.deepCopy()
        WebSocketClient.send("/app/party/publish", party)
    }

    fun editParty(party: FishingParty) {
        party.profileId = User.profileId
        lastParty = party.deepCopy()
        WebSocketClient.send("/app/party/edit", party)
    }

    fun submitParty(party: FishingParty) {
        if (myParty == null) {
            publishParty(party)
        } else {
            editParty(party)
        }
    }

    fun requeueParty() {
        if (!BackendSettings.backendAccepted) {
            Chat.sendMessage(TextUtils.backendAcceptMessage())
            return
        }

        if (!World.isInSkyblock) {
            Chat.sendMessage(
                TextUtils.rfuLiteral(
                    "Must be in skyblock to use this feature!",
                    TextStyle(TextColor.LIGHT_RED, TextEffects.UNDERLINE)
                )
            )
            return
        }

        if (World.isOnAlpha) {
            Chat.sendMessage(
                TextUtils.rfuLiteral(
                    "Party Finder is disabled on the Alpha network!",
                    TextStyle(TextColor.LIGHT_RED, TextEffects.UNDERLINE)
                )
            )
            return
        }

        if (!WebSocketClient.isConnected) {
            Chat.sendMessage(TextUtils.rfupfLiteral("Not connected to RFU Backend!", TextColor.LIGHT_RED))
            return
        }

        if (myParty != null) {
            Chat.sendMessage(TextUtils.rfupfLiteral("You already have an active party listing!", TextColor.LIGHT_RED))
            return
        }

        val party = lastParty?.deepCopy() ?: run {
            val entry = UIPartyPresetsModal.getPresetsEntry()
            entry.lastPartyState?.let { state ->
                val blank = FishingParty.blankParty()
                state.applyTo(blank)
                blank
            }
        }

        if (party == null) {
            Chat.sendMessage(TextUtils.rfupfLiteral("No previous party to requeue!", TextColor.LIGHT_RED))
            return
        }

        party.profileId = User.profileId
        party.user = User.getUsername()
        party.players.current = if (Party.inParty) maxOf(Party.memberCount, 1) else 1

        val validation = PartyRequirementsManager.canCreateParty(party)
        if (!validation.isSuccess) {
            Chat.sendMessage(TextUtils.rfupfLiteral(validation.getErrorMessage(), TextColor.LIGHT_RED))
            return
        }

        val now = System.currentTimeMillis()
        val elapsed = now - lastSubmitTime
        if (elapsed < 5000L) {
            val secondsLeft = ((5000L - elapsed) / 1000L) + 1
            Chat.sendMessage(TextUtils.rfupfLiteral("Please wait ${secondsLeft}s before trying again!", TextColor.LIGHT_RED))
            return
        }

        if (DevSettings.devMode && DevSettings.isInSkyblock) {
            lastSubmitTime = now
            submitParty(party)
            Chat.sendMessage(TextUtils.rfupfLiteral("Party requeued!", TextColor.LIGHT_GREEN))
        } else {
            Party.requestPartyInfo {
                if (Party.inParty && !Party.isLeader) {
                    Chat.sendMessage(TextUtils.rfupfLiteral("You must be the party leader to do this!", TextColor.LIGHT_RED))
                } else {
                    lastSubmitTime = System.currentTimeMillis()
                    party.players.current = if (Party.inParty) maxOf(Party.memberCount, 1) else 1
                    submitParty(party)
                    Chat.sendMessage(TextUtils.rfupfLiteral("Party requeued!", TextColor.LIGHT_GREEN))
                }
            }
        }
    }

    fun deleteParty(user: String) {
        WebSocketClient.send("/app/party/delete", gson.toJson(user))
        if (user == User.getUsername()) {
            if (myParty != null) {
                myParty?.let { lastParty = it.deepCopy() }
                sendPartyDequeuedMessage()
            }
            myParty = null
        }
    }

    fun joinParty(targetUser: String, profileId: String? = User.profileId) {
        lastJoinTarget = targetUser
        lastJoinTime = Clock.System.now()
        Party.requestedUser = targetUser
        pendingJoinHasError = false
        
        WebSocketClient.send("/app/party/join", JoinPartyRequest(targetUser, profileId))
        
        pendingJoinJob?.cancel()
        pendingJoinJob = Coroutines.launch {
            delay(500)
            if (!pendingJoinHasError) {
                Chat.sendMessage(TextUtils.rfupfLiteral("Sent a join request to $targetUser", TextColor.YELLOW))
            }
        }
    }

    fun reportParty(user: String) {
        WebSocketClient.send("/app/party/report", gson.toJson(user))
    }
}

