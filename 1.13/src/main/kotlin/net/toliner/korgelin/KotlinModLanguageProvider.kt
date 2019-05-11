package net.toliner.korgelin

import net.minecraftforge.forgespi.language.ILifecycleEvent
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import net.toliner.korgelin.api.KotlinMod
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.Type
import java.util.function.Consumer
import java.util.function.Supplier

@Suppress("UNUSED")
class KotlinModLanguageProvider : IModLanguageProvider {

    companion object {
        val logger = LogManager.getLogger(KotlinModLanguageProvider::class.java)
    }

    private val annotationType = Type.getType(KotlinMod::class.java)

    override fun getFileVisitor(): Consumer<ModFileScanData> = Consumer { scanResult ->
        scanResult.annotations.asSequence()
                .filter { it.annotationType == annotationType }
                .map { KotlinModTarget(it.classType.className, it.annotationData["id"] as String) }
                .onEach { logger.debug("Found @KotlinMod class ${it.className} with id ${it.modId}") }
                .associateBy(KotlinModTarget::modId)
                .let { scanResult.addLanguageLoader(it) }
    }

    override fun <R : ILifecycleEvent<R>?> consumeLifecycleEvent(consumeEvent: Supplier<R>?) {

    }

    override fun name() = "korgelin"
}