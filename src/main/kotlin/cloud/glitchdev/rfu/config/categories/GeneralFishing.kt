package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.config.SoundTitleSettings
import cloud.glitchdev.rfu.config.TitleSettings
import cloud.glitchdev.rfu.constants.fishing.FishTrackingType
import cloud.glitchdev.rfu.constants.fishing.LiquidTypes
import cloud.glitchdev.rfu.data.mob.DeployableType
import cloud.glitchdev.rfu.feature.fishing.DoubleHookMessages
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import java.awt.Color

object GeneralFishing : Category("General Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for all kinds of fishing!")

    init {
        dualSeparator {
            title = "Fish Tracking"
            description = "Track your fishing stats!"
        }
    }

    var fishingTime by int(5) {
        name = Literal("Fishing Downtime Limit")
        description = Literal("The max ammount of downtime for the trackers to reset in minutes, also used as the window (e.g. 5 -> sc/h during last 5 minutes)")
        range = 5..60
        slider = true
    }

    var pauseSessionOnWindowReached by boolean(false) {
        name = Literal("Pause Session on Downtime")
        description = Literal("Makes the fishing display pause instead of resetting when the downtime limit is reached. You need to use /rfuresetsession or restart the game to reset it.")
    }

    var pauseKeybind by key(0) {
        name = Literal("Pause Keybind")
        description = Literal("Keybind to manually pause the fishing session.")
    }

    init {
        dualSeparator {
            title = "Custom Fishing Keybinds"
            description = "Change your keybinds when fishing"
        }
    }

    var overrideFishingKeybinds by reloadableBoolean(false) {
        name = Literal("Override Fishing Keybinds")
        description = Literal("Enable custom keybinds when fishing.")
    }

    var disableOnRareSC by boolean(true) {
        name = Literal("Disable on Rare SC")
        description = Literal("Disables custom keybind overrides when a rare sea creature is nearby.")
        condition = { overrideFishingKeybinds }
    }

    val customBinds by obj(CustomBinds) {
        name = Literal("Fishing Keybinds")
        description = Literal("Overrides hotbar 1-9 and left/right click keybinds while fishing.")
        condition = { overrideFishingKeybinds }
    }

    init {
        dualSeparator {
            title = "Fishing Display"
            description = "Track your xp and scs!"
        }
    }

    var fishTrackingDisplay by reloadableBoolean(true) {
        name = Literal("Toggle")
        description = Literal("Enables the Fishing display")
    }

    var fishTrackingItems by enums(*FishTrackingType.entries.toTypedArray()) {
        name = Literal("Tracking Items")
        description = Literal("Select which items to track in the display. The display is read like this: Window Rate [Overall Rate] (Total)")
        condition = { fishTrackingDisplay }
    }

    var fishTrackingOnlyWhenFishing by boolean(true) {
        name = Literal("Only display when fishing")
        description = Literal("Only show the display when you're fishing")
        condition = { fishTrackingDisplay }
    }

    init {
        dualSeparator {
            title = "Deployables"
            description = "Everything deployables related, Flares, Fluxes, You name it!"
        }
    }

    var deployableDisplay by reloadableBoolean(true) {
        name = Literal("Deployable Display")
        description = Literal("Toggles the deployable display")
    }

    var deployableTimerDisplay by enums(*DeployableType.entries.toTypedArray()) {
        name = Literal("Deployable Timers")
        description = Literal("Select which deployable timers to display.")
        condition = { deployableDisplay }
    }

    val deployable by obj(DeployableTitleSettings()) {
        name = Literal("Deployable Expiry Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("deployable expiry", "deployable", "flare", "flux", "umberella", "title", "subtitle", "duration", "sound", "volume")
    }

    var deployableExpiredPartyMessage by boolean(false) {
        name = Literal("Deployable Expired Party Message")
        description = Literal("Sends a party message when a nearby deployable expires.")
    }

    init {
        dualSeparator {
            title = "Double Hook"
            description = "Double hook shenanigans"
        }
    }

    var toggleDoubleHookMessages by reloadableBoolean(false) {
        name = Literal("Toggle Double Hook Messages")
        description = Literal("Automatically send messages when you get a double hook!")
    }

    var doubleHookMessages by strings(
        "o/ &9~~~~~~~|&f_&9|",
        "o| &9~~~~~~~&c.&9~",
        "o| &9~~~~~~~&c*&9~",
        "o| &9~~~~~~&3<><",
        "o| &9~~~~&3<><&9~~",
        "o| &9~~&3<><&9~~~~",
        "\\o/ &3<><&9~~~~~",
        "( ^_^) &b[ &3<>< &b]",
        "( >_<) &b[ &3RFU &b]"
    ) {
        name = Literal("Double Hook Messages")
        description = Literal("The messages sent to chat when you get a double hook. It will pick one at random.")
        condition = { toggleDoubleHookMessages }
    }

    init {
        previewButton(
            DoubleHookMessages::preview,
            "Preview Message",
            "Shows a preview of one of the double hook messages in chat."
        ) { toggleDoubleHookMessages }
    }

    var randomDoubleHookMessages by boolean(false) {
        name = Literal("Random Double Hook Messages")
        description = Literal("Makes double hook messages random")
        condition = { toggleDoubleHookMessages }
    }

    init {
        dualSeparator {
            title = "Alerts"
            description = "Alerts to help you fish!"
        }
    }

    val failCast by obj(SoundTitleSettings(
        title = "&c&lFailed Cast!",
        duration = 5,
        fadeIn = 0,
        soundId = "rfu:failed_cast"
    )) {
        name = Literal("Failed Cast Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("failed cast", "failcast", "title", "subtitle", "duration", "sound", "volume")
    }

    val noBait by obj(TitleSettings(
        title = "&c&lNO BAIT!"
    )) {
        name = Literal("No Bait Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("no bait", "nobait", "title", "subtitle", "duration")
    }

    val missingRodPiece by obj(TitleSettings(
        title = "&c&lMISSING ROD PIECE!",
        subtitle = "&7{pieces}",
        placeholders = "{pieces}",
        previews = mapOf(
            "Preview" to listOf("pieces" to "Hook, Line")
        )
    )) {
        name = Literal("Missing Rod Piece Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("missing rod piece", "missingrodpiece", "hook", "line", "sinker", "title", "subtitle", "duration")
    }

    val noFishingArmor by obj(TitleSettings(
        title = "&c&lNO FISHING ARMOR!"
    )) {
        name = Literal("No Fishing Armor Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("no fishing armor", "nofishingarmor", "title", "subtitle", "duration")
    }

    val noMobyDuck by obj(TitleSettings(
        title = "&c&lNO MOBY-DUCK!",
        enabledByDefault = false
    )) {
        name = Literal("No Moby-Duck Alert")
        description = Literal("Alerts when fishing without an active Moby-Duck buff. Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("moby duck", "moby-duck", "wisdom", "buff", "title", "subtitle", "duration")
    }

    init {
        dualSeparator {
            title = "Fishing"
            description = "Anything fishing related that didn't fit elsewhere"
        }
    }

    var rodTimerDisplay by reloadableBoolean(false) {
        name = Literal("Rod Timer Display")
        description = Literal("Display the current rod timer on screen")
    }

    var rodTimerReadyText by string("!!!") {
        name = Literal("Rod Timer Ready Text")
        description = Literal("Text shown when the rod timer ends. Supports & color and formatting codes.")
        condition = { rodTimerDisplay }
    }

    var rodTimerSound by boolean(false) {
        name = Literal("Rod Timer Sound")
        description = Literal("Plays a sound when the fishing stream approaches, increasing in pitch.")
    }

    var muteAnnoyingSounds by boolean(false) {
        name = Literal("Mute Annoying Sounds")
        description = Literal("Mutes Ghast Crying in Backwater Bayou and Elder Guardian moans in the Crimson Isle.")
    }

    var bobbinTimeDisplay by reloadableBoolean(true) {
        name = Literal("Bobbin' Time Display")
        description = Literal("Display nearby bobbers and Bobbin' Time bonus on screen")
    }

    var mobyDuckDisplay by reloadableBoolean(true) {
        name = Literal("Moby-Duck Timer Display")
        description = Literal("Display the Moby-Duck Fishing Wisdom buff timer on screen.")
    }

    var showActualBaitCount by boolean(true) {
        name = Literal("Show Actual Bait Count")
        description = Literal("Replaces the stack size in the 9th hotbar slot with the actual remaining bait count.")
    }

    init {
        dualSeparator {
            title = "Custom Water"
            description = "Customize the appearance of water."
        }
    }

    var customWater by reloadableBoolean(false) {
        name = Literal("Custom Water")
        description = Literal("Changes water's appearance, color, and fog.")
    }

    var customWaterLiquid by ObservableEntry(enum(LiquidTypes.LAVA) {
        name = Literal("Liquid Style")
        description = Literal("Pick the liquid style for the custom water.")
        condition = { customWater }
    }) { _, _ ->
        reloadScreen()
    }

    var customWaterCustomColor by reloadableBoolean(false) {
        name = Literal("Custom Colors")
        description = Literal("Overrides the liquid's original colors.")
        condition = { customWater }
    }

    var customWaterColor by color(Color(63, 118, 228).rgb) {
        name = Literal("Liquid Color")
        description = Literal("Changes the color of the liquid.")
        allowAlpha = false
        condition = { customWater && customWaterCustomColor && customWaterLiquid == LiquidTypes.WATER }
    }

    var waterTextureOpacity by float(180f / 255f * 100f) {
        name = Literal("Opacity")
        description = Literal("The opacity of the water.")
        range = 0f..100f
        slider = true
        condition = { customWater && customWaterLiquid == LiquidTypes.WATER }
    }

    var customWaterDarkColor by color(Color(155, 23, 0).rgb) {
        name = Literal("Dark Tones")
        description = Literal("Changes the darkest shades of lava.")
        allowAlpha = false
        condition = { customWater && customWaterCustomColor && customWaterLiquid == LiquidTypes.LAVA }
    }

    var customWaterMidtoneColor by color(Color(255, 100, 0).rgb) {
        name = Literal("Midtones")
        description = Literal("Changes the middle shades and fog of lava.")
        allowAlpha = false
        condition = { customWater && customWaterCustomColor && customWaterLiquid == LiquidTypes.LAVA }
    }

    var customWaterHighlightColor by color(Color(255, 255, 85).rgb) {
        name = Literal("Highlights")
        description = Literal("Changes the brightest shades of lava.")
        allowAlpha = false
        condition = { customWater && customWaterCustomColor && customWaterLiquid == LiquidTypes.LAVA }
    }

    var lavaTextureOpacity by float(100f) {
        name = Literal("Opacity")
        description = Literal("The opacity of the lava.")
        range = 0f..100f
        slider = true
        condition = { customWater && customWaterLiquid == LiquidTypes.LAVA }
    }

    var customWaterDisableFog by boolean(false) {
        name = Literal("Disable Fog")
        description = Literal("Removes the fog while inside the water.")
        condition = { customWater }
    }

    class DeployableTitleSettings : SoundTitleSettings(
        title = "{color}&l{name} Expired!",
        subtitle = "",
        placeholders = "{name}, {color}",
        previews = mapOf(
            "Preview Flare" to listOf("name" to "Flare", "color" to "&6"),
            "Preview Umberella" to listOf("name" to "Umberella", "color" to "&9"),
            "Preview Flux" to listOf("name" to "Flux", "color" to "&d")
        ),
        soundId = "rfu:deployable_expired"
    ) {
        init {
            separator {
                title = "Trigger Options"
                condition = { enabled }
            }
        }

        var types by enums(*DeployableType.entries.toTypedArray()) {
            name = Literal("Deployable Alerts")
            description = Literal("Select which deployables trigger alerts and party messages.")
            condition = { enabled }
        }
    }
}
