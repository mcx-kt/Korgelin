package net.toliner.korgelin.container

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.registries.GameData
import net.minecraftforge.registries.IForgeRegistry
import net.toliner.korgelin.api.BlockAutoRegistrable
import net.toliner.korgelin.api.EnumForgeRegistryType
import net.toliner.korgelin.api.KotlinModContentRegistry
import net.toliner.korgelin.loading
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.Type
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

object AutoRegisterer {
    private val logger = LogManager.getLogger()!!
    private val targetType = Type.getType(KotlinModContentRegistry::class.java)

    @Suppress("UNCHECKED_CAST")
    fun inject(event: RegistryEvent.Register<*>, mod: KotlinModContainer, scanData: ModFileScanData, loader: ClassLoader) {
        when (val name = event.getName()) {
            GameData.ITEMS -> item(event.getRegistry() as IForgeRegistry<Item>, mod, scanData, loader)
            GameData.BLOCKS -> block(event.getRegistry() as IForgeRegistry<Block>, mod, scanData, loader)
            else -> logger.loading("Unsupported registry type: $name")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun item(registry: IForgeRegistry<Item>, mod: KotlinModContainer, scanData: ModFileScanData, loader: ClassLoader) {
        scanData.annotations
                .filter { it.annotationType == targetType }
                .filter { it.annotationData["modId"] == mod.modId }
                .filter { EnumForgeRegistryType.valueOf((it.annotationData["type"] as ModAnnotation.EnumHolder).value) == EnumForgeRegistryType.ITEM }
                .forEach { ad ->
                    runCatching {
                        val targetClass = Class.forName(ad.classType.className, true, loader).kotlin
                        val instance = targetClass.objectInstance
                                ?: throw IllegalStateException("@KotlinContentRegistry class must object declaration.")
                        targetClass.declaredMemberProperties
                                .mapNotNull {
                                    runCatching { it as KProperty1<Any, Item>? }
                                            .getOrDefault(null)?.get(instance)
                                }
                                .forEach { registry.register(it) }
                    }.onFailure {
                        logger.loading("Failed to load mod class ${ad.classType} for @KotlinContentRegistry annotation", it)
                    }
                }
    }

    @Suppress("UNCHECKED_CAST")
    private fun block(registry: IForgeRegistry<Block>, mod: KotlinModContainer, scanData: ModFileScanData, loader: ClassLoader) {
        scanData.annotations
                .filter { it.annotationType == targetType }
                .filter { it.annotationData["modId"] == mod.modId }
                .filter { EnumForgeRegistryType.valueOf((it.annotationData["type"] as ModAnnotation.EnumHolder).value) == EnumForgeRegistryType.BLOCK }
                .forEach { ad ->
                    runCatching {
                        val targetClass = Class.forName(ad.classType.className, true, loader).kotlin
                        val instance = targetClass.objectInstance
                                ?: throw IllegalStateException("@KotlinContentRegistry class must object declaration.")
                        targetClass.declaredMemberProperties
                                .mapNotNull {
                                    runCatching { it as KProperty1<Any, Block>? }
                                            .getOrDefault(null)?.get(instance)
                                }
                                .forEach { registry.register(it) }
                    }.onFailure {
                        logger.loading("Failed to load mod class ${ad.classType} for @KotlinContentRegistry annotation", it)
                    }
                }
    }
}