package cloud.glitchdev.rfu.utils.dsl

import cloud.glitchdev.rfu.utils.User

fun String.removeFormatting() : String {
    return this.replace("§.".toRegex(), "")
}

fun String.removeRankTag() : String {
    return this.replace("""\[[A-Z]+\+*\]""".toRegex(), "").trim()
}

fun String.toExactRegex() : Regex {
    return """^$this$""".toRegex()
}

fun String.isUser() : Boolean {
    return User.isUser(this)
}