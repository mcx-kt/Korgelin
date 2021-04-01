package net.toliner.korgelin.container

import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.IForgeRegistry
import net.toliner.korgelin.api.EnumForgeRegistryType
import net.toliner.korgelin.api.IAutoRegistrableBlock
import net.toliner.korgelin.api.KotlinModContentRegistry
import net.toliner.korgelin.loading
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.Type
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

internal object AutoRegisterer {
    private val logger = LogManager.getLogger()!!
    private val targetType = Type.getType(KotlinModContentRegistry::class.java)
    private val itemBlocks = mutableMapOf<String, MutableList<BlockItem>>()

    @Suppress("UNCHECKED_CAST")
    fun inject(event: RegistryEvent.Register<*>, mod: KotlinModContainer, scanData: ModFileScanData, loader: ClassLoader) {
        when (val name = event.getName()) {
            ForgeRegistries.ITEMS.registryName -> item(event.getRegistry() as IForgeRegistry<Item>, mod, scanData, loader)
            ForgeRegistries.Keys.BLOCKS.registryName -> block(event.getRegistry() as IForgeRegistry<Block>, mod, scanData, loader)
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
        val list = itemBlocks[mod.modId]
        if (list != null) {
            for (itemBlock in list) {
                registry.register(itemBlock)
            }
            itemBlocks.remove(mod.modId)
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
                                .onEach {
                                    if (it !is IAutoRegistrableBlock) return@onEach
                                    registry.register(it)
                                    val itemBlock = it.itemBlock
                                            ?: BlockItem(it, it.itemBlockProperty).apply { registryName = it.registryName }
                                    itemBlocks.getOrPut(mod.modId, ::mutableListOf) += itemBlock
                                }
                    }.onFailure {
                        logger.loading("Failed to load mod class ${ad.classType} for @KotlinContentRegistry annotation", it)
                    }
                }
    }
}