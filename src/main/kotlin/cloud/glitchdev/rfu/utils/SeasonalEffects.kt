package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.config.categories.OtherSettings
import java.time.LocalDate
import java.time.Month

object SeasonalEffects {
    private val isOctober
        get() = LocalDate.now().month == Month.OCTOBER

    val halloweenActive: Boolean
        get() = OtherSettings.seasonalEffects && isOctober
}
