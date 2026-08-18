package cloud.glitchdev.rfu.model.party

data class JoinPartyRequest(
    val targetUser: String,
    val profileId: String? = null
)
