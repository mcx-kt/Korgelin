package net.toliner.korgelin.container

import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.LifecycleEventProvider
import net.minecraftforge.fml.Logging.LOADING
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModLoadingException
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import net.toliner.korgelin.loading
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.reflect.KClass

class KotlinModContainer(
        info: IModInfo,
        val modClassLoader: ClassLoader,
        val scanResult: ModFileScanData,
        val className: String
) : ModContainer(info) {

    companion object {
        val logger = LogManager.getLogger(KotlinModContainer::class.java)!!
    }

    // Properties  ================================================================================
    private lateinit var modInstance: Any
    private lateinit var modClass: KClass<*>
    val eventBus = BusBuilder.builder().setExceptionHandler(this::onEventFailed).setTrackPhases(false).build()!!

    // Event Handlers  ============================================================================
    val beforeEvent = fun(_: LifecycleEventProvider.LifecycleEvent) {

    }

    val afterEvent = fun(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
        if (currentState == ModLoadingStage.ERROR) {
            logger.error("An error occurred while dispathing event ${lifecycleEvent.fromStage()} to ${getModId()}")
        }
    }

    val fireEvent = fun(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
        val event = lifecycleEvent.getOrBuildEvent(this)
        logger.loading("Firing event for modid ${getModId()} : $event")
        try {
            eventBus.post(event)
            logger.loading("Fired event for modid ${getModId()} : $event")
        } catch (e: Throwable) {
            logger.error(LOADING, "Caught exception during event $event dispatch for modid ${getModId()}")
            throw ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.errorduringevent", e)
        }
    }

    val preinitMod = fun(_: LifecycleEventProvider.LifecycleEvent) {

    }

    val constructMod = fun(event: LifecycleEventProvider.LifecycleEvent) {
        try {
            logger.loading("Loading mod instance $modId of type $className")
            Class.forName(className, true, modClassLoader)  // Invoke this to initialize class.
            modInstance = modClass.objectInstance
                    ?: throw IllegalStateException("KotlinModClass must be object declaration.")
            logger.loading("Loaded mod instance $modId of type $className")
        } catch (e: Throwable) {
            logger.error(LOADING, "Failed to load mod instance. ModID: ${getModId()}, class $className", e)
            throw ModLoadingException(modInfo, event.fromStage(), "fml.modloading.failedtoloadmod", e, modClass)
        }
        logger.loading("Injecting Automatic event subscribers for ${getModId()}")
        KotlinAutomaticEventSucscriber.inject(this, this.scanResult, this.modClass.java.classLoader)
        logger.loading("Completed Automatic event subscribers for ${getModId()}")
    }

    val initMod = fun(_: LifecycleEventProvider.LifecycleEvent) {

    }

    val completeLoading = fun(_: LifecycleEventProvider.LifecycleEvent) {

    }

    @Suppress("UNCHECKED_CAST")
    val loadRegistries = fun(event: LifecycleEventProvider.LifecycleEvent) {
        val registerEvent = event.getOrBuildEvent(this) as RegistryEvent.Register<*>
        logger.loading("Injecting automatic registerer for ${getModId()}")
        AutoRegisterer.inject(registerEvent, this, scanResult, modClassLoader)
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
        createTrigger(ModLoadingStage.CONSTRUCT, constructMod, false)
        createTrigger(ModLoadingStage.CREATE_REGISTRIES)
        createTrigger(ModLoadingStage.LOAD_REGISTRIES, loadRegistries)
        createTrigger(ModLoadingStage.COMMON_SETUP, preinitMod)
        createTrigger(ModLoadingStage.SIDED_SETUP)
        createTrigger(ModLoadingStage.ENQUEUE_IMC, initMod)
        createTrigger(ModLoadingStage.PROCESS_IMC)
        createTrigger(ModLoadingStage.COMPLETE, completeLoading)
        configHandler = Optional.of(Consumer { event -> this.eventBus.post(event) })
    }

    // Overrides ==================================================================================

    override fun matches(mod: Any?) = mod == modInstance

    override fun getMod() = modInstance

    // Methods ====================================================================================

    private fun createTrigger(stage: ModLoadingStage, handler: ((lifecycleEvent: LifecycleEventProvider.LifecycleEvent) -> Unit)? = null, fireEvent: Boolean = true) {
        triggerMap[stage] = Consumer(beforeEvent)
                .let { if (handler != null) it.andThen(handler) else it }
                .let { if (fireEvent) it.andThen(this.fireEvent) else it }
                .andThen(afterEvent)
    }

    fun onEventFailed(eventBus: IEventBus, event: Event, listeners: Array<IEventListener>, i: Int, throwable: Throwable) {
        logger.error(EventBusErrorMessage(event, i, listeners, throwable))
    }
}
