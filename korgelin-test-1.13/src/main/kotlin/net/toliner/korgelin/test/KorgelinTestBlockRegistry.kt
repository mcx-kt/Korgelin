package net.toliner.korgelin.test

import net.minecraft.block.material.Material
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.world.World
import net.toliner.korgelin.api.BlockAutoRegistrable
import net.toliner.korgelin.api.EnumForgeRegistryType
import net.toliner.korgelin.api.KotlinModContentRegistry

@KotlinModContentRegistry("korgelin-test", EnumForgeRegistryType.BLOCK)
object KorgelinTestBlockRegistry {
    val exampleBlock = object : BlockAutoRegistrable(Properties.create(Material.ROCK)) {
        override val itemBlockProperty = Item.Properties().group(ItemGroup.DECORATIONS)

        init {
            setRegistryName("korgelin-test", "example_block")
        }
    }

    val exampleBlockCustomIB = object : BlockAutoRegistrable(Properties.create(Material.ROCK)) {

        override val itemBlockProperty: Item.Properties = Item.Properties().group(ItemGroup.DECORATIONS)

        init {
            setRegistryName("korgelin-test", "example_block_custom_ib")
            this.itemBlock = object : ItemBlock(this, itemBlockProperty) {
                init {
                    setRegistryName("korgelin-test", "example_block_custom_ib")
                }

                override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<ITextComponent>, flagIn: ITooltipFlag) {
                    super.addInformation(stack, worldIn, tooltip, flagIn)
                    tooltip.add(TextComponentString("Hello, World!"))
                }
            }
        }
    }
}