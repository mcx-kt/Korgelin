package net.toliner.korgelin.test

import net.minecraft.item.Item
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemGroup
import net.toliner.korgelin.EnumForgeRegistryType
import net.toliner.korgelin.KotlinModContentRegistry

@KotlinModContentRegistry("korgelin-test", EnumForgeRegistryType.ITEM)
object KorgelinTestItemRegistry {
    val exampleItem = Item(Item.Properties().group(ItemGroup.MISC)).setRegistryName("korgelin-test", "example_tem")!!
    val exmapleFood = ItemFood(5, 5.0f, false, Item.Properties().group(ItemGroup.FOOD)).setRegistryName("korgelin-test", "example_food")!!
}