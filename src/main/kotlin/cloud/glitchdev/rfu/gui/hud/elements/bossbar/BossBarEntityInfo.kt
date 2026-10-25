package cloud.glitchdev.rfu.gui.hud.elements.bossbar

data class BossBarEntityInfo(
    val sbName: String,
    val healthStr: String,
    val maxHealthStr: String,
    val health: Long,
    val maxHealth: Long,
    val shurikenCount: Int,
    val outdatedNametag: Boolean,
    val count: Int,
    val showCount: Boolean
)
