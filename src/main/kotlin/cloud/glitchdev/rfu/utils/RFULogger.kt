package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.config.categories.DevSettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

object RFULogger {
    private val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    @JvmStatic
    @JvmOverloads
    fun dev(message: String, prefix: String? = "[RFU Dev]") {
        if (DevSettings.devMode) {
            debug(message, prefix)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun dev(message: String, exception: Throwable, prefix: String? = "[RFU Dev]") {
        if (DevSettings.devMode) {
            debug(message, exception, prefix)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun info(message: String, prefix: String? = "[RFU]") {
        log(message, Level.INFO, prefix)
    }

    @JvmStatic
    @JvmOverloads
    fun warn(message: String, prefix: String? = "[RFU]") {
        log(message, Level.WARN, prefix)
    }

    @JvmStatic
    @JvmOverloads
    fun error(message: String, prefix: String? = "[RFU]") {
        log(message, Level.ERROR, prefix)
    }

    @JvmStatic
    @JvmOverloads
    fun debug(message: String, prefix: String? = "[RFU]") {
        log(message, Level.INFO, prefix)
    }

    @JvmStatic
    @JvmOverloads
    fun trace(message: String, prefix: String? = "[RFU]") {
        log(message, Level.TRACE, prefix)
    }

    @JvmStatic
    @JvmOverloads
    fun info(message: String, exception: Throwable, prefix: String? = "[RFU]") {
        log(message, exception, Level.INFO, prefix)
    }

    @JvmStatic
    @JvmOverloads
    fun warn(message: String, exception: Throwable, prefix: String? = "[RFU]") {
        log(message, exception, Level.WARN, prefix)
    }

    @JvmStatic
    @JvmOverloads
    fun error(message: String, exception: Throwable, prefix: String? = "[RFU]") {
        log(message, exception, Level.ERROR, prefix)
    }

    @JvmStatic
    @JvmOverloads
    fun debug(message: String, exception: Throwable, prefix: String? = "[RFU]") {
        log(message, exception, Level.DEBUG, prefix)
    }

    @JvmStatic
    @JvmOverloads
    fun trace(message: String, exception: Throwable, prefix: String? = "[RFU]") {
        log(message, exception, Level.TRACE, prefix)
    }

    @JvmStatic
    @JvmOverloads
    fun log(message: String, level: Level = Level.INFO, prefix: String? = "[RFU]") {
        if (DevSettings.devMode) {
            val formattedMessage = "$prefix $message"
            when (level) {
                Level.ERROR -> LOGGER.error(formattedMessage)
                Level.WARN -> LOGGER.warn(formattedMessage)
                Level.INFO -> LOGGER.info(formattedMessage)
                Level.DEBUG -> LOGGER.debug(formattedMessage)
                Level.TRACE -> LOGGER.trace(formattedMessage)
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun log(message: String, exception: Throwable, level: Level = Level.INFO, prefix: String? = "[RFU]") {
        if (DevSettings.devMode) {
            val formattedMessage = "$prefix $message"
            when (level) {
                Level.ERROR -> LOGGER.error(formattedMessage, exception)
                Level.WARN -> LOGGER.warn(formattedMessage, exception)
                Level.INFO -> LOGGER.info(formattedMessage, exception)
                Level.DEBUG -> LOGGER.debug(formattedMessage, exception)
                Level.TRACE -> LOGGER.trace(formattedMessage, exception)
            }
        }
    }
}