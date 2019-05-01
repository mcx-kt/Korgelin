package net.toliner.korgelin

import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

@Mod(
        modid = Korgelin.MOD_ID,
        name = Korgelin.NAME,
        version = Korgelin.VERSION,
        acceptableRemoteVersions = "*",
        acceptedMinecraftVersions = "*",
        modLanguageAdapter = "net.toliner.korgelin.KotlinAdapter"
)
object Korgelin {
    const val MOD_ID = "korgelin"
    const val NAME = "Korgelin"
    const val VERSION = "@VERSION@"

    @Mod.EventHandler
    fun onPreInit(event: FMLPreInitializationEvent) {
        Loader.instance().modList.forEach {
            KorgelinAutomaticEventSubscriber.subscribeAutomatic(it, event.asmData, FMLCommonHandler.instance().side)
        }
    }
}