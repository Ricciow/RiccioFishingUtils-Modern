package cloud.glitchdev.rfu.gui.hud.elements.bossbar

data class BossBarEntityInfo(
    val sbName: String,
    val health: String,
    val maxHealth: String,
    val shurikenCount: Int,
    val outdatedNametag: Boolean,
    val count: Int,
    val showCount: Boolean
)
