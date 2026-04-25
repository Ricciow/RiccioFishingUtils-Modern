package cloud.glitchdev.rfu.feature.ink

import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.constants.text.TextColor.GOLD
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_GREEN
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.data.catches.CatchTracker.catchHistory
import cloud.glitchdev.rfu.data.collections.CollectionItem
import cloud.glitchdev.rfu.data.collections.CollectionsHandler
import cloud.glitchdev.rfu.events.managers.CollectionEvents.registerCollectionUpdateEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@RFUFeature
object InkSessionTracker : Feature {
    val totalInk: Double get() = FishingSession.inkTracker.total
    val currentInkPerHour: Double get() = FishingSession.inkTracker.currentRatePerHour
    
    var sentWarning = false
        private set

    // Session-based squid tracking
    private var squidStart: Long = 0L
    private var nightSquidStart: Long = 0L

    val squidGain: Long get() = SeaCreatures.get("Squid")?.let { catchHistory.getOrAdd(it).total - squidStart } ?: 0L
    val nightSquidGain: Long get() = SeaCreatures.get("Night Squid")?.let { catchHistory.getOrAdd(it).total - nightSquidStart } ?: 0L

    // Goal tracking
    val percentageToGoal: Double
        get() {
            val totalInkColl = CollectionsHandler.get(CollectionItem.INK_SAC)
            val goal = InkFishing.goalInk
            if (goal <= 0) return 0.0
            return (totalInkColl.toDouble() / goal.toDouble()) * 100
        }

    val etaToGoal: Duration
        get() {
            val totalInkColl = CollectionsHandler.get(CollectionItem.INK_SAC)
            val goal = InkFishing.goalInk
            val rate = currentInkPerHour
            if (rate <= 0 || totalInkColl >= goal) return Duration.ZERO
            val diff = goal - totalInkColl
            val hoursNeeded = diff / rate
            return (hoursNeeded * 3600).seconds
        }

    override fun onInitialize() {
        // Initialize start counts
        val squid = SeaCreatures.get("Squid")
        val nightSquid = SeaCreatures.get("Night Squid")

        if (squid == null || nightSquid == null) {
            RFULogger.error("Squid or Night Squid not found in registry during InkSessionTracker initialization!")
            return
        }

        squidStart = catchHistory.getOrAdd(squid).total.toLong()
        nightSquidStart = catchHistory.getOrAdd(nightSquid).total.toLong()

        registerCollectionUpdateEvent { item, amount, _, isSync ->
            if (item == CollectionItem.INK_SAC && amount > 0 && !isSync) {
                handleInk(amount.toDouble())
            }
        }
    }

    @Command
    object ResetCommand : SimpleCommand("rfuresetinkph") {
        override val description: String = "Resets your current Ink session data."

        override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
            resetSession()
            FishingSession.inkTracker.reset()
            FishingSession.inkTracker.update()
            context.source.sendFeedback(
                TextUtils.rfuLiteral("The Ink session data has been reset!", LIGHT_GREEN)
            )
            return 1
        }
    }

    private fun handleInk(ink: Double) {
        if (World.island != FishingIslands.PARK) return
        FishingSession.handleActivity()

        if(CollectionsHandler.get(CollectionItem.INK_SAC) < 1000 && !sentWarning) {
            Chat.sendMessage(TextUtils.rfuLiteral("HINT: ${GOLD}Set your total ink collection by checking your ink ranking in collections menu!", YELLOW, BOLD))
            sentWarning = true
        }

        FishingSession.inkTracker.addEvent(ink)
    }

    fun resetSession() {
        sentWarning = false
        
        // Reset squid tracking to current values
        SeaCreatures.get("Squid")?.let { squidStart = catchHistory.getOrAdd(it).total.toLong() }
        SeaCreatures.get("Night Squid")?.let { nightSquidStart = catchHistory.getOrAdd(it).total.toLong() }
    }

}
