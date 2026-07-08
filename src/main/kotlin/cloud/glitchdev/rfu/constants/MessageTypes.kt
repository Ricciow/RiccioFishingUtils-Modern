package cloud.glitchdev.rfu.constants

enum class MessageTypes(val displayName : String) {
    HYPE("Hyperion"),
    AUTOPET("Autopet"),
    LOOTSHARE("Lootshare"),
    CATCH("Catch"),
    COMBO("Combo"),
    BLOCKS("Blocks in the way"),
    THUNDER_SPARK("Thunder Spark"),
    COCOON("Cocoon"),
    SACKS("Sacks");

    override fun toString(): String {
        return displayName
    }
}