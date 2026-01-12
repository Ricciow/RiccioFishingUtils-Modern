package cloud.glitchdev.rfu.model.announcement

import java.time.Instant

data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    val content: String,
    val issuedAt: Instant
)
