package net.toliner.korgelin.test

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.toliner.korgelin.KotlinEventSubscriber
import org.apache.logging.log4j.LogManager

@KotlinEventSubscriber("korgelin-test")
object EventHandlers {

    private val logger = LogManager.getLogger()!!

    fun setupCommon(event: FMLCommonSetupEvent) {
        logger.info("Hello, this is auto-subscribed CommonSetupEvent!")
    }
}