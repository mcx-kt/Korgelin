package net.toliner.korgelin

import org.apache.logging.log4j.Logger

fun Logger.loading(message: String) = this.info(message)
fun Logger.loading(message: String, exception: Throwable) = this.fatal(message, exception)