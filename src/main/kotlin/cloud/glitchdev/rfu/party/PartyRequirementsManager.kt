package cloud.glitchdev.rfu.party

import cloud.glitchdev.rfu.constants.skyblock.SkillType
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.BooleanEntry
import cloud.glitchdev.rfu.events.managers.EquipmentEvents
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.SkillTracker
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents

@AutoRegister
object PartyRequirementsManager : RegisteredEvent {

    sealed class PartyValidationResult {
        object Success : PartyValidationResult()
        data class Invalid(val failures: List<PartyValidationResult>) : PartyValidationResult()
        data class LevelTooLow(val requiredLevel: Int, val currentLevel: Int) : PartyValidationResult()
        object MissingBrainFood : PartyValidationResult()
        object MissingBloodshot : PartyValidationResult()
        data class MissingRequisite(val requisiteKey: String, val requisiteName: String) : PartyValidationResult()

        val isSuccess: Boolean get() = this is Success

        fun getErrorMessage(): String {
            return when (this) {
                is Success -> ""
                is Invalid -> failures.joinToString("\n\n") { it.getErrorMessage() }
                is LevelTooLow -> "Your Fishing level ($currentLevel) is lower than the required level ($requiredLevel)!\n${TextColor.GRAY}If you are higher level, open the skills tab."
                is MissingBrainFood -> "You must have 5 Brain Food!\n${TextColor.GRAY}If you already have it, open\n${TextColor.GRAY}Sb Menu->Skyblock Leveling->Ways to Level Up->Consumable Tasks"
                is MissingBloodshot -> "You must be wearing a Bloodshot belt!\n${TextColor.GRAY}If you already are, open your equipment menu."
                is MissingRequisite -> "You do not meet the requirement: $requisiteName!"
            }
        }
    }

    var hasBrainFood: Boolean
        get() = (OtherManager.getField("brain_food") as? BooleanEntry)?.value ?: false
        set(value) {
            OtherManager.setField("brain_food", BooleanEntry(value))
            OtherManager.file.save()
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

    fun isLevelSufficient(requiredLevel: Int): Boolean {
        val currentLevel = SkillTracker.getSkillLevel(SkillType.FISHING)
        return currentLevel >= requiredLevel
    }

    fun hasBrainFood(): Boolean {
        return hasBrainFood
    }

    fun hasBloodshotBelt(): Boolean {
        val belt = EquipmentEvents.currentEquipmentSet.belt
        return belt.startsWith("Bloodshot", ignoreCase = true)
    }

    fun validatePartyRequirements(party: FishingParty): PartyValidationResult {
        val failures = mutableListOf<PartyValidationResult>()

        val currentLevel = SkillTracker.getSkillLevel(SkillType.FISHING)
        if (!isLevelSufficient(party.level)) {
            failures.add(PartyValidationResult.LevelTooLow(party.level, currentLevel))
        }

        for (requisite in party.requisites) {
            if (!requisite.value) continue
            when (requisite.id) {
                "brain_food" -> if (!hasBrainFood()) failures.add(PartyValidationResult.MissingBrainFood)
                "bloodshot" -> if (!hasBloodshotBelt()) failures.add(PartyValidationResult.MissingBloodshot)
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

    fun canJoinParty(party: FishingParty): PartyValidationResult = validatePartyRequirements(party)
}
