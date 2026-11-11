package com.rustfurnaces;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RustFurnaces.MODID)
public class ModBlocks {
    public static BlockRustFurnace FURNACE;
    public static BlockRustFurnace ADMIN_FURNACE;
    public static Item FURNACE_ITEM;
    public static Item ADMIN_FURNACE_ITEM;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        FURNACE = new BlockRustFurnace("rust_furnace", false);
        ADMIN_FURNACE = new BlockRustFurnace("admin_furnace", true);
        event.getRegistry().registerAll(FURNACE, ADMIN_FURNACE);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        FURNACE_ITEM = new ItemBlock(FURNACE).setRegistryName(FURNACE.getRegistryName());
        ADMIN_FURNACE_ITEM = new ItemBlock(ADMIN_FURNACE).setRegistryName(ADMIN_FURNACE.getRegistryName());
        event.getRegistry().registerAll(FURNACE_ITEM, ADMIN_FURNACE_ITEM);
    }
}
