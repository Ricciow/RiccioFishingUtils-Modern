package cloud.glitchdev.rfu.model.version
import net.fabricmc.loader.api.Version

data class ModVersion(
    val name: String,
    val version : String,
    val changelog : String
) {
    fun toVersion() : Version {
        return Version.parse(version.substringBefore("+"))
    }
}