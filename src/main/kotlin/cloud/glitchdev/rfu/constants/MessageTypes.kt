package cloud.glitchdev.rfu.constants

enum class MessageTypes(val displayName : String) {
    HYPE("Hyperion"),
    AUTOPET("Autopet"),
    CATCH("Catch");

    override fun toString(): String {
        return displayName
    }
}