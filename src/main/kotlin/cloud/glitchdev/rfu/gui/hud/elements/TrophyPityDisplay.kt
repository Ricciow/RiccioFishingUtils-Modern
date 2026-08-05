package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.TrophyFishing
import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.constants.fishing.Trophy
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.data.fishing.TrophyDataManager
import cloud.glitchdev.rfu.data.fishing.TrophyPityEntry
import cloud.glitchdev.rfu.feature.fishing.TrophyPityTracker
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents.registerTrophyFishCatchEvent
import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents.registerTrophyFrogCatchEvent
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.dsl.isWearingTrophyHunterArmor

@HudElement
object TrophyPityDisplay : AbstractTextHudElement("trophyPity") {
    private const val RECENT_THRESHOLD = 5 * 60 * 1000L // 5 minutes

    override val requirement: Boolean
        get() = TrophyFishing.trophyPityDisplay

    override val isElementActive: Boolean
        get() = getActivePities().isNotEmpty()

    override fun onInitialize() {
        super.onInitialize()
        registerTrophyFrogCatchEvent { _, _, _ -> updateState() }
        registerTrophyFishCatchEvent { _, _, _ -> updateState() }
        registerContainerOpenEvent { _, _, _ -> updateState() }
        registerTickEvent(interval = 20) {
            updateState()
        }
    }

    private fun getActivePities(): List<Pair<TrophyPityEntry, Trophy>> {
        if (!TrophyFishing.showGoldPity && !TrophyFishing.showDiamondPity) return emptyList()

        val island = World.island
        val isFrog = when (island) {
            FishingIslands.ATOLL -> true
            FishingIslands.ISLE -> false
            else -> return emptyList()
        }

        val pityEntries = if (isFrog) TrophyDataManager.data.pity.frogPity else TrophyDataManager.data.pity.fishPity
        val isWearingArmor = isWearingTrophyHunterArmor()
        val now = System.currentTimeMillis()

        return pityEntries.entries
            .asSequence()
            .filter { (_, entry) ->
                (TrophyFishing.showGoldPity && entry.goldProgress > 0) ||
                        (TrophyFishing.showDiamondPity && entry.diamondProgress > 0)
            }
            .filter { (key, _) ->
                !TrophyFishing.displayRecentTrophies ||
                        (now - (TrophyPityTracker.lastCaughtTimes[key] ?: 0L) < RECENT_THRESHOLD)
            }
            .filter { (key, _) ->
                val isExempt =
                        key == TrophyFrog.PUDDLE_JUMPER.name ||
                        key == TrophyFrog.EXPLODING_FROG.name ||
                        key == TrophyFish.GOLDEN_FISH.name
                isWearingArmor || isExempt
            }
            .mapNotNull { (key, entry) ->
                val trophy: Trophy? = if (isFrog) {
                    TrophyFrog.entries.find { it.name == key || it.displayName == entry.name }
                        ?.takeIf { TrophyFishing.displayedTrophyFrogs.contains(it) }
                } else {
                    TrophyFish.entries.find { it.name == key || it.displayName == entry.name }
                        ?.takeIf { TrophyFishing.displayedTrophyFishes.contains(it) }
                }
                trophy?.let { entry to it }
            }
            .sortedWith(compareByDescending<Pair<TrophyPityEntry, Trophy>> { (_, trophy) ->
                trophy.rarity
            }.thenByDescending { (pity, trophy) ->
                val goldPct = if (TrophyFishing.showGoldPity) pity.goldProgress.toFloat() / trophy.goldPity else 0f
                val diamondPct = if (TrophyFishing.showDiamondPity) pity.diamondProgress.toFloat() / trophy.diamondPity else 0f
                maxOf(goldPct, diamondPct)
            }.thenBy { it.first.name })
            .toList()
    }

    override fun onUpdateState() {
        super.onUpdateState()
        val island = World.island
        val isFrog = island == FishingIslands.ATOLL
        val activePities = getActivePities()

        val showPreview = isEditing && (
            activePities.isEmpty() || (island != FishingIslands.ISLE && island != FishingIslands.ATOLL)
        )

        if (showPreview) {
            text.setText(getPreviewText(isFrog))
            return
        }

        if (activePities.isEmpty()) {
            text.setText("")
            return
        }

        val header = if (isFrog) "${TextColor.GOLD}${TextEffects.BOLD}Trophy Frog Pity:" else "${TextColor.GOLD}${TextEffects.BOLD}Trophy Fish Pity:"
        val lines = mutableListOf(header)

        for ((pity, trophy) in activePities) {
            val parts = mutableListOf<String>()
            if (TrophyFishing.showGoldPity) {
                parts.add("${TextColor.GOLD}${pity.goldProgress}/${trophy.goldPity}")
            }
            if (TrophyFishing.showDiamondPity) {
                parts.add("${TextColor.AQUAMARINE}${pity.diamondProgress}/${trophy.diamondPity}")
            }
            lines.add("${trophy.rarity.color}${pity.name}: ${parts.joinToString(" ")}")
        }

        text.setText(lines.joinToString("\n"))
    }

    private fun getPreviewText(isFrog: Boolean): String {
        val parts = mutableListOf<String>()
        if (TrophyFishing.showGoldPity) parts.add("${TextColor.GOLD}50/100")
        if (TrophyFishing.showDiamondPity) parts.add("${TextColor.AQUAMARINE}300/600")
        val suffix = parts.joinToString(" ")

        return buildString {
            if (isFrog) {
                val bfColor = TrophyFrog.BULLFROG.rarity.color
                val rhColor = TrophyFrog.REALITY_HOPPER.rarity.color
                append("${TextColor.GOLD}${TextEffects.BOLD}Trophy Frog Pity Preview:\n")
                append("${bfColor}Bullfrog: $suffix\n")
                append("${rhColor}Reality Hopper: $suffix")
            } else {
                val sfColor = TrophyFish.SLUGFISH.rarity.color
                val bfColor = TrophyFish.BLOBFISH.rarity.color
                append("${TextColor.GOLD}${TextEffects.BOLD}Trophy Fish Pity Preview:\n")
                append("${sfColor}Slugfish: $suffix\n")
                append("${bfColor}Blobfish: $suffix")
            }
        }
    }
}
