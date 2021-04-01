package net.toliner.korgelin.api

import net.minecraft.item.Item
import net.minecraft.item.BlockItem

interface IAutoRegistrableBlock {
    /**
     * The property of ItemBlock.
     */
    val itemBlockProperty: Item.Properties
    /**
     * This is used when you want to use custom ItemBlock.
     * if null, Korgelin will generate default ItemBlock instance using [itemBlockProperty]
     * and registry name of the block.
     */
    val itemBlock: BlockItem?
}