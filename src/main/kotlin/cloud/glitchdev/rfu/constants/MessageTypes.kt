package cloud.glitchdev.rfu.constants

enum class MessageTypes(val displayName : String) {
    HYPE("Hyperion"),
    AUTOPET("Autopet"),
    CATCH("Catch"),
    COMBO("Combo"),
    BLOCKS("Blocks in the way");

    override fun toString(): String {
        return displayName
    }
}