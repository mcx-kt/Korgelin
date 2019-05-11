package net.toliner.korgelin

import org.apache.logging.log4j.Logger

internal fun Logger.loading(message: String) = this.info(message)
internal fun Logger.loading(message: String, exception: Throwable) = this.fatal(message, exception)