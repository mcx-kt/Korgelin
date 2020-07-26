package net.toliner.korgelin.api

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock

/**
 * This is default implementation of [IAutoRegistrableBlock].
 * You should override [itemBlockProperty] because it is different from block to block.
 *
 * @param properties The property of this block.
 */
abstract class BlockAutoRegistrable(properties: Properties) : Block(properties), IAutoRegistrableBlock {

    abstract override val itemBlockProperty: Item.Properties
    /**
     * Default is null because most blocks don't need custom ItemBlock.
     * You can't override this.
     * You should set instance of your custom ItemBlock in constructor.
     * @see IAutoRegistrableBlock.itemBlock
     */
    final override var itemBlock: ItemBlock? = null
        protected set
}