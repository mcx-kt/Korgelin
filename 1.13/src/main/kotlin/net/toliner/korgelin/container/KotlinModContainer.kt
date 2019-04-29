package net.toliner.korgelin.container

import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.*
import net.minecraftforge.fml.Logging.LOADING
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.reflect.KClass

class KotlinModContainer(
        info: IModInfo,
        modClassLoader: ClassLoader,
        val scanResult: ModFileScanData,
        val className: String
) : ModContainer(info) {

    companion object {
        val logger = LogManager.getLogger(KotlinModContainer::class.java)
    }

    // Properties  ================================================================================
    private lateinit var modInstance: Any
    private lateinit var modClass: KClass<*>
    val eventBus = BusBuilder.builder().setExceptionHandler(this::onEventFailed).setTrackPhases(false).build()

    // Event Handlers  ============================================================================
    private val beforeEvent = fun(_: LifecycleEventProvider.LifecycleEvent) {

    }

    private val afterEvent = fun(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
        if (currentState == ModLoadingStage.ERROR) {
            logger.error("An error occurred while dispathing event ${lifecycleEvent.fromStage()} to ${getModId()}")
        }
    }

    private val fireEvent = fun(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
        val event = lifecycleEvent.getOrBuildEvent(this)
        logger.debug(LOADING, "Firing event for modid ${getModId()} : $event")
        try {
            eventBus.post(event)
            logger.debug(LOADING, "Fired event for modid ${getModId()} : $event")
        } catch (e: Throwable) {
            logger.error(LOADING, "Caught exception during event $event dispatch for modid ${getModId()}")
            throw ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.errorduringevent", e)
        }
    }

    private val preinitMod = fun(_: LifecycleEventProvider.LifecycleEvent) {

    }

    private val constructMod = fun(event: LifecycleEventProvider.LifecycleEvent) {
        try {
            logger.debug(LOADING, "Loading mod instance $modId of type $className")
            Class.forName(className)  // Invoke this to initialize class.
            modInstance = modClass.objectInstance
                    ?: throw IllegalStateException("KotlinModClass must bbe object declaration.")
            logger.debug(LOADING, "Loaded mod instance $modId of type $className")
        } catch (e: Throwable) {
            logger.error(LOADING, "Failed to load mod instance. ModID: ${getModId()}, class $className")
            throw ModLoadingException(modInfo, event.fromStage(), "fml.modloading.failedtoloadmod", e, modClass)
        }
        logger.debug(LOADING, "Injecting Automatic event subscribers for ${getModId()}")
        // @SubscribeEventのついた処理をEventBusに登録する部分？
        AutomaticEventSubscriber.inject(this, this.scanResult, this.modClass.java.classLoader)
        logger.debug(LOADING, "Completed Automatic event subscribers for ${getModId()}")
    }

    private val initMod = fun(_: LifecycleEventProvider.LifecycleEvent) {

    }

    private val completeLoading = fun(_: LifecycleEventProvider.LifecycleEvent) {

    }

    // Constructor ================================================================================

    init {
        logger.debug("Creating KotlinModContainer instance for $className with classLoader $modClassLoader & ${KotlinModContainer::class.java.classLoader}")
        contextExtension = Supplier { KotlinModLoadingContext(this) }
        createTrigger(ModLoadingStage.CONSTRUCT, constructMod, false)
        createTrigger(ModLoadingStage.CREATE_REGISTRIES)
        createTrigger(ModLoadingStage.LOAD_REGISTRIES)
        createTrigger(ModLoadingStage.COMMON_SETUP, preinitMod)
        createTrigger(ModLoadingStage.SIDED_SETUP)
        createTrigger(ModLoadingStage.ENQUEUE_IMC, initMod)
        createTrigger(ModLoadingStage.PROCESS_IMC)
        createTrigger(ModLoadingStage.COMPLETE, completeLoading)
        configHandler = Optional.of(Consumer { event -> this.eventBus.post(event) })
        try {
            modClass = Class.forName(className, false, modClassLoader).kotlin
            logger.debug(LOADING, "Loaded modclass $className with $modClassLoader")
        } catch (e: Throwable) {
            logger.error(LOADING, "Failed to load class $className", e)
            throw ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", e)
        }
    }

    // Overrides ==================================================================================

    override fun matches(mod: Any?) = mod == modInstance

    override fun getMod() = modInstance

    // Methods ====================================================================================

    private fun createTrigger(stage: ModLoadingStage, handler: ((lifecycleEvent: LifecycleEventProvider.LifecycleEvent) -> Unit)? = null, fireEvent: Boolean = true) {
        triggerMap[stage] = Consumer(beforeEvent)
                .apply { if (handler != null) andThen(handler) }
                .apply { if (fireEvent) andThen(this@KotlinModContainer.fireEvent) }
                .andThen(afterEvent)
    }

    private fun onEventFailed(eventBus: IEventBus, event: Event, listeners: Array<IEventListener>, i: Int, throwable: Throwable) {
        logger.error(EventBusErrorMessage(event, i, listeners, throwable))
    }
}
