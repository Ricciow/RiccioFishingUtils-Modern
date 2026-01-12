package cloud.glitchdev.rfu.config.dev

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry

@Category("Developer Settings")
object DevSettings {
    @JvmField
    @ConfigEntry(id = "Dev Mode")
    @Comment("Enable developer mode")
    var devMode : Boolean = false
}