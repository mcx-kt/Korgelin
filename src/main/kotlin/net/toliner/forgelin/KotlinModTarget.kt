package net.toliner.forgelin

import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData

internal class KotlinModTarget(val className: String, val modId: String) : IModLanguageProvider.IModLanguageLoader {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> loadMod(info: IModInfo, modClassLoader: ClassLoader, modFileScanResults: ModFileScanData): T {
        return KotlinModContainer(info, modClassLoader, modFileScanResults, className) as T
    }
}
