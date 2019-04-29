package net.toliner.korgelin

import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.toliner.korgelin.container.KotlinModLoadingContext
import org.apache.logging.log4j.LogManager

@KotlinMod("korgelin-test")
object KotlinTestMod {

    private val logger = LogManager.getLogger()!!

    init {
        KotlinModLoadingContext.get().eventBus.apply {
            addListener<FMLCommonSetupEvent> { setupCommon(it) }
            addListener(setupClient)
            register(this@KotlinTestMod)
        }
    }

    fun setupCommon(event: FMLCommonSetupEvent) {
        logger.info("Hello from setup")
    }

    val setupClient = fun(_: FMLClientSetupEvent) {
        logger.info("Hello from Client!")
    }

    @SubscribeEvent
    fun setupServer(event: FMLDedicatedServerSetupEvent) {
        logger.info("Hello from Server!")
    }
}