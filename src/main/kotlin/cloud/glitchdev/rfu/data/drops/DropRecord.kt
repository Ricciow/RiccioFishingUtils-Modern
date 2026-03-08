package cloud.glitchdev.rfu.data.drops

import kotlin.time.Clock
import kotlin.time.Instant

class DropRecord(
    var totalCount : Int,
    var sinceCount : Int?,
    var magicFind : Int? = null
) {
    var date : Instant = Clock.System.now()
}