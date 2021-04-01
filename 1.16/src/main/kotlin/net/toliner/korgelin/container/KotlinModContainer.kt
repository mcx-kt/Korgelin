package net.toliner.korgelin.container

import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.Logging.LOADING
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModLoadingException
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent
import net.minecraftforge.fml.event.lifecycle.IModBusEvent
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import net.toliner.korgelin.loading
import org.apache.logging.log4j.LogManager
import java.util.function.Supplier
import kotlin.reflect.KClass

class KotlinModContainer(
    info: IModInfo,
    private val modClassLoader: ClassLoader,
    private val scanResult: ModFileScanData,
    private val className: String
) : ModContainer(info) {

    companion object {
        val logger = LogManager.getLogger(KotlinModContainer::class.java)!!
    }

    // Properties  ================================================================================
    private lateinit var modInstance: Any
    private var modClass: KClass<*>
    val eventBus = BusBuilder.builder().setExceptionHandler(this::onEventFailed).setTrackPhases(false).markerType(IModBusEvent::class.java).build()!!

    // Event Handlers  ============================================================================

    private fun constructMod(event: FMLConstructModEvent) {
        try {
            logger.loading("Loading mod instance $modId of type $className")
            Class.forName(className, true, modClassLoader)  // Invoke this to initialize class.
            modInstance = modClass.objectInstance
                    ?: throw IllegalStateException("KotlinModClass must be object declaration.")
            logger.loading("Loaded mod instance $modId of type $className")
        } catch (e: Throwable) {
            logger.error(LOADING, "Failed to load mod instance. ModID: ${getModId()}, class $className", e)
            throw ModLoadingException(modInfo, this.modLoadingStage, "fml.modloading.failedtoloadmod", e, modClass)
        }
        logger.loading("Injecting Automatic event subscribers for ${getModId()}")
        KotlinAutomaticEventSubscriber.inject(this, this.scanResult, this.modClass.java.classLoader)
        logger.loading("Completed Automatic event subscribers for ${getModId()}")
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadRegistries(event: RegistryEvent.Register<*>) {
        logger.loading("Injecting automatic registerer for ${getModId()}")
        AutoRegisterer.inject(event, this, scanResult, modClassLoader)
        logger.loading("Completed automatic registering for ${getModId()}")
    }

    // Constructor ================================================================================

    init {
        logger.loading("Creating KotlinModContainer instance for $className with classLoader $modClassLoader & ${KotlinModContainer::class.java.classLoader}")
        try {
            modClass = Class.forName(className, false, modClassLoader).kotlin
            logger.loading("Loaded modclass $className with $modClassLoader")
        } catch (e: Throwable) {
            logger.loading("Failed to load class $className", e)
            throw ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", e)
        }
        contextExtension = Supplier { KotlinModLoadingContext(this) }
    }

    // Overrides ==================================================================================

    override fun matches(mod: Any?) = mod == modInstance

    override fun getMod() = modInstance

    // Methods ====================================================================================

    private fun onEventFailed(eventBus: IEventBus, event: Event, listeners: Array<IEventListener>, i: Int, throwable: Throwable) {
        logger.error(EventBusErrorMessage(event, i, listeners, throwable))
    }

    override fun <T> acceptEvent(event: T) where T : Event, T : IModBusEvent {
        try {
            logger.loading("Firing event for modid ${getModId()} : $event")
            when(event) {
                is FMLConstructModEvent -> constructMod(event)
                is RegistryEvent.Register<*> -> loadRegistries(event)
            }
            eventBus.post(event)
            logger.loading("Fired event for modid ${getModId()} : $event")
        } catch (e: Exception) {
            logger.error(LOADING, "Caught exception during event $event dispatch for modid ${getModId()}")
            throw ModLoadingException(modInfo, modLoadingStage, "fml.modloading.errorduringevent", e)
        }
    }
}
