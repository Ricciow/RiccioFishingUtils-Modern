package cloud.glitchdev.rfu.constants

object Skills {
    /**
     * XP required to reach each level (from the previous level).
     * Index 0 is for Level 1, index 59 is for Level 60.
     */
    val XP_REQUIRED_FOR_LEVEL = listOf(
        50L, 125L, 200L, 300L, 500L, 750L, 1000L, 1500L, 2000L, 3500L,
        5000L, 7500L, 10000L, 15000L, 20000L, 30000L, 50000L, 75000L, 100000L, 200000L,
        300000L, 400000L, 500000L, 600000L, 700000L, 800000L, 900000L, 1000000L, 1100000L, 1200000L,
        1300000L, 1400000L, 1500000L, 1600000L, 1700000L, 1800000L, 1900000L, 2000000L, 2100000L, 2200000L,
        2300000L, 2400000L, 2500000L, 2600000L, 2750000L, 2900000L, 3100000L, 3400000L, 3700000L, 4000000L,
        4300000L, 4600000L, 4900000L, 5200000L, 5500000L, 5800000L, 6100000L, 6400000L, 6700000L, 7000000L
    )

    /**
     * Total XP accumulated before reaching the current level.
     * Index 0 is the total before Level 1 (which is 0).
     * Index 50 is the total before Level 51 (Total XP for Level 50).
     */
    val TOTAL_XP_ACCUMULATED_PREVIOUS_LEVEL = listOf(
        0L, 50L, 175L, 375L, 675L, 1175L, 1925L, 2925L, 4425L, 6425L,
        9925L, 14925L, 22425L, 32425L, 47425L, 67425L, 97425L, 147425L, 222425L, 322425L,
        522425L, 822425L, 1222425L, 1722425L, 2322425L, 3022425L, 3822425L, 4722425L, 5722425L, 6822425L,
        8022425L, 9322425L, 10722425L, 12222425L, 13822425L, 15522425L, 17322425L, 19222425L, 21222425L, 23322425L,
        25522425L, 27822425L, 30222425L, 32722425L, 35322425L, 38072425L, 40972425L, 44072425L, 47472425L, 51172425L,
        55172425L, 59472425L, 64072425L, 68972425L, 74172425L, 79672425L, 85472425L, 91572425L, 97972425L, 104672425L, 111672425L
    )

    const val MAX_LEVEL = 60
    val TOTAL_XP_MAX_LEVEL = TOTAL_XP_ACCUMULATED_PREVIOUS_LEVEL.last()

    /**
     * Returns the XP required to reach the given level from the previous one.
     */
    fun getRequiredXpForLevel(level: Int): Long {
        if (level !in 1..MAX_LEVEL) return 0L
        return XP_REQUIRED_FOR_LEVEL[level - 1]
    }

    /**
     * Returns the total accumulated XP exactly when the given level is reached.
     * Level 0 returns 0.
     */
    fun getTotalXpAtLevel(level: Int): Long {
        if (level !in 0..MAX_LEVEL) return 0L
        return TOTAL_XP_ACCUMULATED_PREVIOUS_LEVEL[level]
    }

    fun parseXp(str: String): Long {
        var s = str.replace(",", "").trim()
        val multiplier = when {
            s.endsWith("k", ignoreCase = true) -> {
                s = s.dropLast(1)
                1_000L
            }
            s.endsWith("M", ignoreCase = true) -> {
                s = s.dropLast(1)
                1_000_000L
            }
            s.endsWith("B", ignoreCase = true) -> {
                s = s.dropLast(1)
                1_000_000_000L
            }
            else -> 1L
        }
        return (s.toDoubleOrNull()?.let { it * multiplier } ?: 0.0).toLong()
    }

    fun calculateTotalXp(currentXp: Long, requiredXp: Long): Long {
        return if (requiredXp == 0L) {
            getTotalXpAtLevel(50) + currentXp
        } else {
            val index = XP_REQUIRED_FOR_LEVEL.indexOf(requiredXp)
            if (index != -1) {
                getTotalXpAtLevel(index) + currentXp
            } else {
                0L
            }
        }
    }
}
