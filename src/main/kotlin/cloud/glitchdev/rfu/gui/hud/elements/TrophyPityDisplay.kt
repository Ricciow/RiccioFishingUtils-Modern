package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
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
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
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
        get() {
            val island = World.island
            if (!TrophyFishing.showGoldPity && !TrophyFishing.showDiamondPity) return false
            val now = System.currentTimeMillis()
            val isWearingArmor = isWearingTrophyHunterArmor()
            
            return when (island) {
                FishingIslands.ISLE -> {
                    isWearingArmor && TrophyDataManager.data.pity.fishPity.entries.any { (key, it) ->
                        ( (TrophyFishing.showGoldPity && it.goldProgress > 0) || (TrophyFishing.showDiamondPity && it.diamondProgress > 0) ) && 
                        (now - (TrophyPityTracker.lastCaughtTimes[key] ?: 0L) < RECENT_THRESHOLD)
                    }
                }
                FishingIslands.ATOLL -> {
                    TrophyDataManager.data.pity.frogPity.entries.any { (key, it) ->
                        val isExempt = key == TrophyFrog.PUDDLE_JUMPER.name || key == TrophyFrog.EXPLODING_FROG.name
                        (isWearingArmor || isExempt) &&
                        ( (TrophyFishing.showGoldPity && it.goldProgress > 0) || (TrophyFishing.showDiamondPity && it.diamondProgress > 0) ) && 
                        (now - (TrophyPityTracker.lastCaughtTimes[key] ?: 0L) < RECENT_THRESHOLD)
                    }
                }
                else -> false
            }
        }

    override fun onInitialize() {
        super.onInitialize()
        registerTickEvent(interval = 20) {
            updateState()
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()
        val island = World.island

        val isFrog = island == FishingIslands.ATOLL

        val pityEntries = if (isFrog) {
            TrophyDataManager.data.pity.frogPity
        } else {
            TrophyDataManager.data.pity.fishPity
        }

        val now = System.currentTimeMillis()
        val isWearingArmor = isWearingTrophyHunterArmor()
        
        val showPreview = isEditing && (
            pityEntries.none { (key, it) -> 
                val isExempt = key == TrophyFrog.PUDDLE_JUMPER.name || key == TrophyFrog.EXPLODING_FROG.name
                (isWearingArmor || (isFrog && isExempt)) && 
                ( (TrophyFishing.showGoldPity && it.goldProgress > 0) || (TrophyFishing.showDiamondPity && it.diamondProgress > 0) ) && 
                (now - (TrophyPityTracker.lastCaughtTimes[key] ?: 0L) < RECENT_THRESHOLD) 
            } ||
            (island != FishingIslands.ISLE && island != FishingIslands.ATOLL)
        )

        if (showPreview) {
            val parts = mutableListOf<String>()
            if (TrophyFishing.showGoldPity) parts.add("${TextColor.GOLD}50/100")
            if (TrophyFishing.showDiamondPity) parts.add("${TextColor.AQUAMARINE}300/600")
            val suffix = parts.joinToString(" ")

            val preview = buildString {
                if (isFrog) {
                    val bfColor = TrophyFrog.entries.find { it.displayName == "Bullfrog" }?.rarity?.color ?: TextColor.WHITE
                    val rhColor = TrophyFrog.entries.find { it.displayName == "Reality Hopper" }?.rarity?.color ?: TextColor.WHITE
                    append("${TextColor.GOLD}${TextEffects.BOLD}Trophy Frog Pity Preview:\n")
                    append("${bfColor}Bullfrog: $suffix\n")
                    append("${rhColor}Reality Hopper: $suffix")
                } else {
                    val sfColor = TrophyFish.entries.find { it.displayName == "Slugfish" }?.rarity?.color ?: TextColor.WHITE
                    val bfColor = TrophyFish.entries.find { it.displayName == "Blobfish" }?.rarity?.color ?: TextColor.WHITE
                    append("${TextColor.GOLD}${TextEffects.BOLD}Trophy Fish Pity Preview:\n")
                    append("${sfColor}Slugfish: $suffix\n")
                    append("${bfColor}Blobfish: $suffix")
                }
            }
            text.setText(preview)
            return
        }

        if (!isElementActive) {
            text.setText("")
            return
        }

        val activePities = pityEntries.entries
            .asSequence()
            .filter { (_, it) ->
                (TrophyFishing.showGoldPity && it.goldProgress > 0) ||
                        (TrophyFishing.showDiamondPity && it.diamondProgress > 0)
            }
            .filter { (key, _) -> now - (TrophyPityTracker.lastCaughtTimes[key] ?: 0L) < RECENT_THRESHOLD }
            .filter { (key, entry) ->
                val isExempt = key == TrophyFrog.PUDDLE_JUMPER.name || key == TrophyFrog.EXPLODING_FROG.name
                if (!isWearingArmor && !isExempt) return@filter false

                if (isFrog) {
                    val frog = TrophyFrog.entries.find { it.displayName == entry.name }
                    frog != null && TrophyFishing.displayedTrophyFrogs.contains(frog)
                } else {
                    val fish = TrophyFish.entries.find { it.displayName == entry.name }
                    fish != null && TrophyFishing.displayedTrophyFishes.contains(fish)
                }
            }
            .map { entry ->
                val trophy: Trophy = if (isFrog) {
                    TrophyFrog.entries.find { it.displayName == entry.value.name }!!
                } else {
                    TrophyFish.entries.find { it.displayName == entry.value.name }!!
                }
                entry.value to trophy
            }
            .sortedWith(compareByDescending<Pair<TrophyPityEntry, Trophy>> { (pity, trophy) ->
                val goldPct = if (TrophyFishing.showGoldPity) pity.goldProgress.toFloat() / trophy.goldPity else 0f
                val diamondPct = if (TrophyFishing.showDiamondPity) pity.diamondProgress.toFloat() / trophy.diamondPity else 0f
                maxOf(goldPct, diamondPct)
            }.thenBy { it.first.name })
            .toList()

        if (activePities.isEmpty()) {
            text.setText("")
            return
        }

        val lines = mutableListOf<String>()
        if (isFrog) {
            lines.add("${TextColor.GOLD}${TextEffects.BOLD}Trophy Frog Pity:")
        } else {
            lines.add("${TextColor.GOLD}${TextEffects.BOLD}Trophy Fish Pity:")
        }

        for ((pity, trophy) in activePities) {
            val trophyColor = trophy.rarity.color
            val pityText = buildString {
                append("$trophyColor${pity.name}: ")
                val parts = mutableListOf<String>()
                if (TrophyFishing.showGoldPity) {
                    parts.add("${TextColor.GOLD}${pity.goldProgress}/${trophy.goldPity}")
                }
                if (TrophyFishing.showDiamondPity) {
                    parts.add("${TextColor.AQUAMARINE}${pity.diamondProgress}/${trophy.diamondPity}")
                }
                append(parts.joinToString(" "))
            }
            lines.add(pityText)
        }

        text.setText(lines.joinToString("\n"))
    }
}
