package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.constants.skyblock.SkillType
import cloud.glitchdev.rfu.constants.skyblock.Skills
import cloud.glitchdev.rfu.data.skills.SkillsData
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.SkillEvents
import cloud.glitchdev.rfu.utils.dsl.toSkillLevel
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents

@AutoRegister
object SkillTracker : RegisteredEvent {
    private val jsonFile = JsonFile(
        filename = "skills_tracker.json",
        type = SkillsData::class.java,
        defaultFactory = { SkillsData() }
    )

    private val ACTION_BAR_REGEX = """\+([0-9,]+(?:\.[0-9]+)?)\s+(Combat|Farming|Foraging|Fishing|Mining|Enchanting|Alchemy|Carpentry|Taming|Hunting)\s+\((?:([^/()]+)/([^()]+)|([0-9]+(?:\.[0-9]+)?)%)\)""".toRegex(RegexOption.IGNORE_CASE)
    private val lastPercentageGains = mutableMapOf<SkillType, Triple<String, String, Int>>()
    private val SKILL_ITEM_NAME_REGEX = """^(Combat|Farming|Foraging|Fishing|Mining|Enchanting|Alchemy|Carpentry|Taming|Hunting)\s+([IVXLCDM]+|\d+)$""".toRegex(RegexOption.IGNORE_CASE)
    private val LORE_LEVEL_REGEX = """Progress to Level\s+([IVXLCDM]+|\d+)""".toRegex(RegexOption.IGNORE_CASE)
    private val LORE_PROGRESS_REGEX = """(?<![0-9,.])([0-9][0-9,]*(?:\.[0-9]+)?[kMB]?)\s*/\s*([0-9][0-9,]*(?:\.[0-9]+)?[kMB]?)""".toRegex(RegexOption.IGNORE_CASE)
    private val LORE_TOTAL_XP_REGEX = """([0-9,]{5,}(?:\.[0-9]+)?)""".toRegex()

    override fun register() {
        registerGameEvent(ACTION_BAR_REGEX, isOverlay = true) { msg, _, matches ->
            if (matches == null) return@registerGameEvent
            val gainedXpStr = matches.groupValues[1]
            val skillName = matches.groupValues[2]
            val currentXpStr = matches.groupValues.getOrNull(3)
            val requiredXpStr = matches.groupValues.getOrNull(4)

            val skill = SkillType.fromName(skillName) ?: return@registerGameEvent
            val currentTotal = getSkillXp(skill)

            val cur = if (!currentXpStr.isNullOrBlank()) Skills.parseXp(currentXpStr) else null
            val req = if (!requiredXpStr.isNullOrBlank()) Skills.parseXp(requiredXpStr) else null
            val gained = Skills.parseXp(gainedXpStr)

            val newTotal = if (cur != null && req != null) {
                jsonFile.data.skillLevelMap[skill.name.lowercase()] =
                    if (req == 0L) skill.maxLevel else getLevelFromNextLevelXp(req) - 1
                calculateTotalXp(skill, cur, req)
            } else {
                val percentage = matches.groupValues[5].toDoubleOrNull() ?: return@registerGameEvent
                if (percentage !in 0.0..100.0) return@registerGameEvent
                calculatePercentageXp(skill, percentage) ?: run {
                    val percentageStr = matches.groupValues[5]
                    val previousGain = lastPercentageGains[skill]
                    val repeat = if (previousGain?.first == percentageStr && previousGain.second == gainedXpStr) {
                        (previousGain.third + 1) % 3
                    } else 0
                    lastPercentageGains[skill] = Triple(percentageStr, gainedXpStr, repeat)
                    if (repeat != 0) return@registerGameEvent
                    currentTotal + gained
                }
            }

            setSkillXp(skill, newTotal)
        }

        registerDisconnectEvent { lastPercentageGains.clear() }

        registerContainerOpenEvent { _, _, items ->
            for (item in items) {
                val name = item.hoverName.toUnformattedString()
                val match = SKILL_ITEM_NAME_REGEX.find(name) ?: continue
                val skillName = match.groupValues[1]
                val skill = SkillType.fromName(skillName) ?: continue
                val level = parseLevel(match.groupValues[2])
                if (level !in 0..skill.maxLevel) continue

                val loreLines = item[DataComponents.LORE]?.lines?.map { it.toUnformattedString() } ?: continue
                val loreText = loreLines.joinToString(" ")

                var parsedTotalXp: Long? = null

                if (loreText.contains("Max Skill level reached!", ignoreCase = true)) {
                    val totalXpMatch = LORE_TOTAL_XP_REGEX.findAll(loreText).lastOrNull()
                    if (totalXpMatch != null) {
                        parsedTotalXp = Skills.parseXp(totalXpMatch.groupValues[1])
                    }
                } else {
                    val levelMatch = LORE_LEVEL_REGEX.find(loreText)
                    val progressMatch = LORE_PROGRESS_REGEX.find(loreText)
                    
                    if (levelMatch != null && progressMatch != null) {
                        val nextLevelStr = levelMatch.groupValues[1]
                        val nextLevel = parseLevel(nextLevelStr)
                        val currentLevel = nextLevel - 1
                        val currentXp = Skills.parseXp(progressMatch.groupValues[1])
                        val requiredXp = Skills.parseXp(progressMatch.groupValues[2])
                        if (nextLevel in 1..skill.maxLevel && nextLevel == level + 1 &&
                            requiredXp == Skills.getRequiredXpForLevel(nextLevel) &&
                            currentXp in 0..requiredXp
                        ) {
                            parsedTotalXp = xpRequiredForLevel(currentLevel) + currentXp
                        }
                    }
                }

                if (parsedTotalXp != null) {
                    jsonFile.data.skillLevelMap[skill.name.lowercase()] = level
                    lastPercentageGains.remove(skill)
                    setSkillXp(skill, parsedTotalXp)
                }
            }
        }
    }

    fun getSkillXp(skill: SkillType): Long {
        return jsonFile.data.skillXpMap[skill.name.lowercase()] ?: 0L
    }

    fun setSkillXp(skill: SkillType, xp: Long) {
        val oldXp = getSkillXp(skill)
        if (xp == oldXp) return
        jsonFile.data.skillXpMap[skill.name.lowercase()] = xp
        jsonFile.save()
        SkillEvents.runTasks(skill, xp)
    }

    fun getSkillLevel(skill: SkillType): Int {
        return getSkillXp(skill).toSkillLevel()
    }

    fun xpRequiredForLevel(desiredLevel: Int): Long = Skills.getTotalXpAtLevel(desiredLevel)

    private fun calculatePercentageXp(skill: SkillType, percentage: Double): Long? {
        val key = skill.name.lowercase()
        var level = jsonFile.data.skillLevelMap[key] ?: return null
        if (level == skill.maxLevel) return getSkillXp(skill)
        if (level !in 0 until skill.maxLevel) return null

        val previousPercentage = (getSkillXp(skill) - xpRequiredForLevel(level)) * 100.0 /
            Skills.getRequiredXpForLevel(level + 1)
        if (percentage + 0.01 < previousPercentage) level++
        jsonFile.data.skillLevelMap[key] = level
        val totalXp = xpRequiredForLevel(level) + if (level < skill.maxLevel) {
            (Skills.getRequiredXpForLevel(level + 1) * percentage / 100.0).toLong()
        } else 0L
        return maxOf(getSkillXp(skill), totalXp)
    }

    private fun getXpForLevelIndex(index: Int): Long {
        return Skills.XP_REQUIRED_FOR_LEVEL.getOrNull(index) ?: 4_000_000L
    }

    private fun calculateXPForCurrentLevel(level: Int): Long {
        return getXpForLevelIndex(level)
    }

    private fun calculateXPToNextLevel(currentLevel: Int): Long {
        val xpForCurrentLevel = getXpForLevelIndex(currentLevel)
        val xpForNextLevel = getXpForLevelIndex(currentLevel + 1)
        return xpForNextLevel - xpForCurrentLevel
    }

    private fun getLevelFromNextLevelXp(neededXp: Long): Int {
        for (l in 1..60) {
            if (Skills.getRequiredXpForLevel(l) == neededXp) {
                return l
            }
        }
        var l = 60
        var xpForNext = 7_000_000L + 600_000L
        var slope = 600_000L
        while (xpForNext <= neededXp) {
            if (xpForNext == neededXp) {
                return l + 1
            }
            l++
            xpForNext += slope
            if (l % 10 == 0) slope *= 2
        }
        return 60
    }

    private fun calculateTotalXp(skill: SkillType, currentXp: Long, neededXp: Long): Long {
        if (neededXp == 0L) {
            return Skills.getTotalXpAtLevel(skill.maxLevel) + currentXp
        }
        val targetLevel = getLevelFromNextLevelXp(neededXp)
        return xpRequiredForLevel(targetLevel - 1) + currentXp
    }

    fun romanToDecimal(roman: String): Int {
        var sum = 0
        var lastValue = 0
        for (i in roman.length - 1 downTo 0) {
            val char = roman[i].uppercaseChar()
            val value = when (char) {
                'I' -> 1
                'V' -> 5
                'X' -> 10
                'L' -> 50
                'C' -> 100
                'D' -> 500
                'M' -> 1000
                else -> 0
            }
            if (value < lastValue) {
                sum -= value
            } else {
                sum += value
            }
            lastValue = value
        }
        return sum
    }

    fun parseLevel(str: String): Int {
        val trimmed = str.trim()
        return trimmed.toIntOrNull() ?: romanToDecimal(trimmed)
    }
}
