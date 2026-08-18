package cloud.glitchdev.rfu.model.party

data class PlayerRequisitesResult(
    val requisites: Map<String, Boolean> = emptyMap(),
    val level: Int = 0
)
