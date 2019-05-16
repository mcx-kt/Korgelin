package net.toliner.korgelin

import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData

internal class KotlinModTarget(val className: String, val modId: String) : IModLanguageProvider.IModLanguageLoader {

    private val logger = KotlinModLanguageProvider.logger

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> loadMod(info: IModInfo, modClassLoader: ClassLoader, modFileScanResults: ModFileScanData): T {
        try {
            val containerClass = Thread.currentThread().contextClassLoader.loadClass("net.toliner.korgelin.container.KotlinModContainer")
            logger.loading("Loading KotlinModContainer from classloader $${Thread.currentThread().contextClassLoader} - got ${containerClass.classLoader}")
            val constructor = containerClass.getConstructor(IModInfo::class.java, ClassLoader::class.java, ModFileScanData::class.java, String::class.java)
            return constructor.newInstance(info, modClassLoader, modFileScanResults, className) as T
        } catch (e: Throwable) {
            logger.loading("Unable to load KotlinModContainer, wut?", e)
            throw RuntimeException(e)
        }
    }
}
