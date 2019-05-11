package net.toliner.korgelin.test

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.toliner.korgelin.api.EnumForgeRegistryType
import net.toliner.korgelin.api.KotlinModContentRegistry

@KotlinModContentRegistry("korgelin-test", EnumForgeRegistryType.BLOCK)
object KorgelinTestBlockRegistry {
    val exampleBlock = Block(Block.Properties.create(Material.ROCK)).setRegistryName("korgelin-test", "example_block")!!
}