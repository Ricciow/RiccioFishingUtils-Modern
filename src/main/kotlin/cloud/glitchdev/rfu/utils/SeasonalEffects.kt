package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.config.categories.OtherSettings
import java.time.LocalDate
import java.time.Month

object SeasonalEffects {
    private val isHalloweenSeason: Boolean
        get() {
            val today = LocalDate.now()
            return (today.month == Month.OCTOBER && today.dayOfMonth >= 30) ||
                (today.month == Month.NOVEMBER && today.dayOfMonth == 1)
        }

    val halloweenActive: Boolean
        get() = OtherSettings.seasonalEffects && isHalloweenSeason
}
