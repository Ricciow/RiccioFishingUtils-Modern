package cloud.glitchdev.rfu.data.other.data

data class MobyDuckEntry(
    var remainingTicks: Long = 0L,
    var isCollectorsEdition: Boolean = false,
    var wisdomValue: String = "1"
) : Entry

