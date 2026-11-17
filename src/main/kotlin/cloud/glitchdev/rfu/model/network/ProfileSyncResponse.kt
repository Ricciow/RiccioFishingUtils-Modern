package cloud.glitchdev.rfu.model.network

import cloud.glitchdev.rfu.utils.User.sameProfileId

data class ProfileSyncResponse(
    val playerUuid: String? = null,
    val previousProfileId: String? = null,
    val profileId: String? = null
) {
    fun canApplyTo(currentPlayerUuid: String, currentProfileId: String?): Boolean =
        profileId?.replace("-", "")?.matches(Regex("[a-fA-F0-9]{32}")) == true &&
            sameProfileId(playerUuid, currentPlayerUuid) &&
            (sameProfileId(previousProfileId, currentProfileId) || sameProfileId(profileId, currentProfileId))
}
