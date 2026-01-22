package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.config.categories.DevSettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

object RFULogger {
    private val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    fun dev(message: String) {
        if (DevSettings.devMode) {
            debug(message, "[RFU Dev]")
        }
    }

    fun dev(message: String, exception: Throwable) {
        if (DevSettings.devMode) {
            debug(message, exception, "[RFU Dev]")
        }
    }

    fun info(message: String, prefix: String? = "[RFU]") {
        log(message, Level.INFO, prefix)
    }

    fun warn(message: String, prefix: String? = "[RFU]") {
        log(message, Level.WARN, prefix)
    }

    fun error(message: String, prefix: String? = "[RFU]") {
        log(message, Level.ERROR, prefix)
    }

    fun debug(message: String, prefix: String? = "[RFU]") {
        log(message, Level.DEBUG, prefix)
    }

    fun trace(message: String, prefix: String? = "[RFU]") {
        log(message, Level.TRACE, prefix)
    }

    fun info(message: String, exception: Throwable, prefix: String? = "[RFU]") {
        log(message, exception, Level.INFO, prefix)
    }

    fun warn(message: String, exception: Throwable, prefix: String? = "[RFU]") {
        log(message, exception, Level.WARN, prefix)
    }

    fun error(message: String, exception: Throwable, prefix: String? = "[RFU]") {
        log(message, exception, Level.ERROR, prefix)
    }

    fun debug(message: String, exception: Throwable, prefix: String? = "[RFU]") {
        log(message, exception, Level.DEBUG, prefix)
    }

    fun trace(message: String, exception: Throwable, prefix: String? = "[RFU]") {
        log(message, exception, Level.TRACE, prefix)
    }

    fun log(message: String, level: Level = Level.INFO, prefix: String? = "[RFU]") {
        if (DevSettings.devMode) {
            val message = "$prefix $message"
            when (level) {
                Level.ERROR -> LOGGER.error(message)
                Level.WARN -> LOGGER.warn(message)
                Level.INFO -> LOGGER.info(message)
                Level.DEBUG -> LOGGER.debug(message)
                Level.TRACE -> LOGGER.trace(message)
            }
        }
    }

    fun log(message: String, exception: Throwable, level: Level = Level.INFO, prefix: String? = "[RFU]") {
        if (DevSettings.devMode) {
            val message = "$prefix $message"
            when (level) {
                Level.ERROR -> LOGGER.error(message, exception)
                Level.WARN -> LOGGER.warn(message, exception)
                Level.INFO -> LOGGER.info(message, exception)
                Level.DEBUG -> LOGGER.debug(message, exception)
                Level.TRACE -> LOGGER.trace(message, exception)
            }
        }
    }
}