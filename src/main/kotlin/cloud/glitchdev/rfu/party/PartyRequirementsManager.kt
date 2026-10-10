package cloud.glitchdev.rfu.party

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.skyblock.SkillType
import cloud.glitchdev.rfu.constants.skyblock.Skills
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.BooleanEntry
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents
import cloud.glitchdev.rfu.model.party.FishingParty
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents
import cloud.glitchdev.rfu.model.party.PlayerRequisitesResult
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.SkillTracker
import cloud.glitchdev.rfu.utils.dsl.compact
import kotlin.time.Clock
import kotlin.time.Instant

@AutoRegister
object PartyRequirementsManager : RegisteredEvent {
    private var playerRequisites: PlayerRequisitesResult? = null
    var lastRequisitesFetchTime: Instant? = null
        private set
    var cachedProfileId: String? = null
        private set

    sealed class PartyValidationResult {
        object Success : PartyValidationResult()
        data class Invalid(val failures: List<PartyValidationResult>) : PartyValidationResult()
        object MissingBrainFood : PartyValidationResult()
        data class MissingRequisite(val requisiteKey: String, val requisiteName: String) : PartyValidationResult()

        val isSuccess: Boolean get() = this is Success

        fun getErrorMessage(): String {
            return when (this) {
                is Success -> ""
                is Invalid -> failures.joinToString("\n\n") { it.getErrorMessage() }
                is MissingBrainFood -> "You must have 5 Brain Food!\n${TextColor.GRAY}If you already have it, open\n${TextColor.GRAY}Sb Menu->Skyblock Leveling->Ways to Level Up->Consumable Tasks"
                is MissingRequisite -> "You do not meet the requirement: $requisiteName!"
            }
        }
    }

    fun updatePlayerRequisites(result: PlayerRequisitesResult, profileId: String? = User.profileId) {
        playerRequisites = result
        cachedProfileId = profileId
        lastRequisitesFetchTime = Clock.System.now()
        PartyFinderEvents.refreshParties()
    }

    fun clearPlayerRequisites() {
        playerRequisites = null
        cachedProfileId = null
        lastRequisitesFetchTime = null
        PartyFinderEvents.refreshParties()
    }

    fun hasEnderman9(): Boolean {
        return playerRequisites?.requisites?.get("enderman_9") ?: false
    }

    fun hasLooting5(): Boolean {
        return playerRequisites?.requisites?.get("looting_5") ?: false
    }

    var hasBrainFood: Boolean
        get() = (OtherManager.getField("brain_food") as? BooleanEntry)?.value ?: false
        set(value) {
            OtherManager.setField("brain_food", BooleanEntry(value))
            OtherManager.file.save()
            PartyFinderEvents.refreshParties()
        }

    override fun register() {
        registerContainerOpenEvent { containerName, _, items ->
            if (containerName == "Tasks ➜ Consumables") {
                val brainFoodItem = items.find { it.hoverName.toUnformattedString().contains("Brain Food", ignoreCase = true) }
                    ?: return@registerContainerOpenEvent
                val lore = brainFoodItem[DataComponents.LORE] ?: return@registerContainerOpenEvent
                for (line in lore.lines()) {
                    val plainText = line.toUnformattedString()
                    if (plainText.contains("Total Progress: 100%", ignoreCase = true)) {
                        if (!hasBrainFood) {
                            hasBrainFood = true
                        }
                        break
                    }
                }
            }
        }
    }

    fun hasBrainFood(): Boolean {
        return hasBrainFood
    }

    fun hasBloodshotBelt(): Boolean {
        return playerRequisites?.requisites?.get("bloodshot") ?: false
    }

    fun hasWitherImpact(): Boolean {
        return playerRequisites?.requisites?.get("wither_impact") ?: false
    }

    fun validatePartyRequirements(party: FishingParty): PartyValidationResult {
        val failures = mutableListOf<PartyValidationResult>()

        if (!meetsFishingRequirements(party)) {
            val level = party.level.coerceIn(0, 50)
            val overflow = party.requiredOverflow()
            val requirement = "Fishing $level" +
                if (level == 50 && overflow > 0) " + ${overflow.compact()} overflow XP" else ""
            failures.add(PartyValidationResult.MissingRequisite("fishing_xp", requirement))
        }

        for (requisite in party.requisites) {
            if (!requisite.value) continue
            when (requisite.id) {
                "brain_food" -> if (!hasBrainFood()) failures.add(PartyValidationResult.MissingBrainFood)
                "bloodshot" -> if (!hasBloodshotBelt()) failures.add(PartyValidationResult.MissingRequisite("bloodshot", requisite.name))
                "enderman_9" -> if (!hasEnderman9()) failures.add(PartyValidationResult.MissingRequisite("enderman_9", requisite.name))
                "looting_5" -> if (!hasLooting5()) failures.add(PartyValidationResult.MissingRequisite("looting_5", requisite.name))
                "wither_impact" -> if (!hasWitherImpact()) failures.add(PartyValidationResult.MissingRequisite("wither_impact", requisite.name))
            }
        }

        return if (failures.isEmpty()) {
            PartyValidationResult.Success
        } else if (failures.size == 1) {
            failures.first()
        } else {
            PartyValidationResult.Invalid(failures)
        }
    }

    fun canCreateParty(party: FishingParty): PartyValidationResult = validatePartyRequirements(party)

    private fun meetsFishingRequirements(party: FishingParty): Boolean {
        val cachedLevelXp = Skills.getTotalXpAtLevel((playerRequisites?.level ?: 0).coerceIn(0, Skills.MAX_LEVEL))
        val fishingXp = maxOf(SkillTracker.getSkillXp(SkillType.FISHING), cachedLevelXp)
        if (fishingXp < Skills.getTotalXpAtLevel(party.level.coerceIn(0, 50))) return false
        return party.level < 50 || fishingXp - Skills.getTotalXpAtLevel(50) >= party.requiredOverflow()
    }

    fun canJoinParty(party: FishingParty): PartyValidationResult = validatePartyRequirements(party)
}

