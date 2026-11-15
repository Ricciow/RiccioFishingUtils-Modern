package cloud.glitchdev.rfu.model.party

import cloud.glitchdev.rfu.utils.User.sameProfileId

data class PlayerRequisitesResult(
    val requisites: Map<String, Boolean> = emptyMap(),
    val level: Int = 0,
    val profileId: String? = null,
    val requestedProfileId: String? = null,
    val bobbinTimeLevel: Int = 0
) {
    fun canApplyTo(currentProfileId: String?): Boolean =
        profileId == null || sameProfileId(currentProfileId, profileId) || sameProfileId(currentProfileId, requestedProfileId)
}
