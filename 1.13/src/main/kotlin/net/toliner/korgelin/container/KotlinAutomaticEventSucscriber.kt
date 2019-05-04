package net.toliner.korgelin.container

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.LogMarkers
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation
import net.minecraftforge.forgespi.language.ModFileScanData
import net.toliner.korgelin.KotlinEventSubscriber
import net.toliner.korgelin.loading
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.Type

object KotlinAutomaticEventSucscriber {
    val logger = LogManager.getLogger()!!
    val targetAnnotation = Type.getType(KotlinEventSubscriber::class.java)

    @Suppress("UNCHECKED_CAST")
    fun inject(mod: KotlinModContainer, scanData: ModFileScanData, loader: ClassLoader) {
        logger.debug(LogMarkers.LOADING, "Attempting to inject @KotlinEventSubscriber classes into the eventbus for $mod")
        scanData.annotations
                .filter { targetAnnotation == it.annotationType }
                .forEach { ad ->
                    val sides = (ad.annotationData["target"] as List<ModAnnotation.EnumHolder>)
                            .map { Dist.valueOf(it.value) }
                    val modId = ad.annotationData["modId"] as String
                    val bus = Mod.EventBusSubscriber.Bus.valueOf((ad.annotationData["bus"] as ModAnnotation.EnumHolder).value)
                    if (mod.modId == modId && sides.contains(FMLEnvironment.dist)) {
                        try {
                            logger.loading("Auto-subscribing ${ad.classType.className} to $bus")
                            val clazz = Class.forName(ad.classType.className, true, loader)
                            bus.bus().get().register(clazz.kotlin.objectInstance ?: clazz)
                        } catch (e: Exception) {
                            logger.loading("Failed to load mod class ${ad.classType} for @KotlinEventSubscriber annotation", e)
                        }
                    }
                }
    }
}