package net.toliner.korgelin.api

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.registries.IForgeRegistry

/**
 * This is an annotation for Mod Main class.
 * If you use Korgelin, you have to use this instead of [Mod]
 *
 * @param id The modId of your mod.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class KotlinMod(val id: String)

/**
 * This is an annotation for event subscribers.
 * **Note:this is recommended to use to object-declaration **
 * If you use this to object declaration, the instance will be registered to [bus].
 * This means that all non-static functions will subscribe events.
 * If you use this to normal class, the class will be registered to [bus]
 * This means that all static functions will subscribe events.
 *
 * @param modId The modId of your mod. This must same as [KotlinMod.id]
 * @param bus The [Mod.EventBusSubscriber.Bus] you want to register.
 * @param target The array of side you want to register this.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class KotlinEventSubscriber(
        val modId: String,
        val bus: Mod.EventBusSubscriber.Bus = Mod.EventBusSubscriber.Bus.FORGE,
        val target: Array<Dist> = [Dist.CLIENT, Dist.DEDICATED_SERVER]
)

/**
 * This is an annotation for your mod's content registry.
 * The content registry must object declaration.
 * All contents of the content registry will be automatically registered to [IForgeRegistry].
 *
 * @param modId The modId of your mod. This must same as [KotlinMod.id]
 * @param type The type of your registry. Only one type(e.g. [net.minecraft.item.Item])
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class KotlinModContentRegistry(
        val modId: String,
        val type: EnumForgeRegistryType
)
