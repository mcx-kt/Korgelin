package net.toliner.korgelin

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.Logger

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class KotlinMod(val id: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class KotlinEventSubscriber(
        val modId: String,
        val bus: Mod.EventBusSubscriber.Bus = Mod.EventBusSubscriber.Bus.FORGE,
        val target: Array<Dist> = [Dist.CLIENT, Dist.DEDICATED_SERVER]
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class KotlinModContentRegistry(
        val modId: String,
        val type: EnumForgeRegistryType
)

fun Logger.loading(message: String) = this.info(message)
fun Logger.loading(message: String, exception: Throwable) = this.fatal(message, exception)