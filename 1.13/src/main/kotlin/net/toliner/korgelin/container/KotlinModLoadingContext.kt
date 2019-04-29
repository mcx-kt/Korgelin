package net.toliner.korgelin.container

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext

class KotlinModLoadingContext(private val container: KotlinModContainer) {
    companion object {
        @JvmStatic
        fun get(): KotlinModLoadingContext = ModLoadingContext.get().extension()
    }

    val eventBus: IEventBus
        get() = container.eventBus
}