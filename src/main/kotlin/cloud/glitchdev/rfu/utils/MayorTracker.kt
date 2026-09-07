package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.constants.skyblock.Mayors
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.utils.network.Network
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.World.SBDay
import cloud.glitchdev.rfu.utils.World.SBHour
import cloud.glitchdev.rfu.utils.World.SBMonth
import cloud.glitchdev.rfu.utils.World.SBYear
import com.google.gson.JsonParser
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

@AutoRegister
object MayorTracker : RegisteredEvent {
    @Volatile
    var currentMayor: Mayors = Mayors.UNKNOWN
        private set

    private const val BASE_RETRY_DELAY_MS = 10_000L
    private const val MAX_RETRY_DELAY_MS = 15 * 60 * 1000L

    @Volatile
    private var lastFetchedYear = -1L
    @Volatile
    private var lastFetchedRealTime = 0L
    @Volatile
    private var consecutiveFailures = 0
    @Volatile
    private var nextRetryTime = 0L
    @Volatile
    private var isFetching = false

    override fun register() {
        registerJoinEvent {
            checkAndFetch()
        }

        registerTickEvent(interval = 200L) {
            checkAndFetch()
        }
    }

    private fun checkAndFetch() {
        if (isFetching) return
        if (System.currentTimeMillis() < nextRetryTime) return

        val isPastElection = SBMonth >= 3 && SBDay >= 27 && SBHour >= 1

        if (lastFetchedYear < SBYear && !isPastElection) {
            fetchMayor()
        }
    }

    @Command
    object MayorCommand : AbstractCommand("rfumayor") {
        override val description: String = "Check current SkyBlock mayor or refresh the data."

        override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
            builder
                .executes { context ->
                    val mayor = currentMayor.mayorName
                    context.source.sendFeedback(TextUtils.rfuLiteral("Current Mayor: ", TextStyle(TextColor.GOLD))
                        .append(Component.literal("${TextColor.YELLOW}$mayor")))
                    1
                }
                .then(lit("refresh").executes { context ->
                    fetchMayor(force = true)
                    context.source.sendFeedback(TextUtils.rfuLiteral("Refreshing mayor data...", TextStyle(TextColor.GRAY)))
                    1
                })
        }
    }

    private fun fetchMayor(force: Boolean = false) {
        if (isFetching) return
        if (!force && System.currentTimeMillis() < nextRetryTime) return

        isFetching = true
        lastFetchedRealTime = System.currentTimeMillis()
        Network.getRequest("https://api.hypixel.net/v2/resources/skyblock/election") { response ->
            if (response.isSuccessful() && response.body != null) {
                try {
                    val json = JsonParser.parseString(response.body).asJsonObject
                    if (json.has("mayor")) {
                        val mayorJson = json.getAsJsonObject("mayor")
                        val name = mayorJson.get("name").asString
                        handleFetchSuccess(name)
                    } else {
                        handleFetchFailure("Mayor API response does not contain 'mayor' object")
                    }
                } catch (e: Exception) {
                    handleFetchFailure("Error parsing mayor API", e)
                }
            } else {
                handleFetchFailure("Error getting mayor API: ${response.body ?: "status code ${response.statusCode}"}")
            }
        }
    }

    private fun handleFetchSuccess(name: String) {
        currentMayor = Mayors.fromName(name)
        lastFetchedYear = SBYear
        consecutiveFailures = 0
        nextRetryTime = 0L
        isFetching = false
        RFULogger.dev("Fetched current Mayor: ${currentMayor.mayorName}")
    }

    private fun handleFetchFailure(message: String, exception: Throwable? = null) {
        consecutiveFailures++
        val shift = (consecutiveFailures - 1).coerceIn(0, 30)
        val waitTime = (BASE_RETRY_DELAY_MS * (1L shl shift)).coerceAtMost(MAX_RETRY_DELAY_MS)
        nextRetryTime = System.currentTimeMillis() + waitTime
        isFetching = false

        val waitSeconds = waitTime / 1000
        if (exception != null) {
            RFULogger.error("$message (retrying in ${waitSeconds}s)", exception)
        } else {
            RFULogger.error("$message (retrying in ${waitSeconds}s)")
        }
    }
}
