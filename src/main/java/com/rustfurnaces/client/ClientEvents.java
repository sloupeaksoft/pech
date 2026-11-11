package com.rustfurnaces.client;

import com.rustfurnaces.ModBlocks;
import com.rustfurnaces.RustFurnaces;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = RustFurnaces.MODID, value = Side.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // Обычная печь выглядит как печь MineRust.
        ModelLoader.setCustomModelResourceLocation(ModBlocks.FURNACE_ITEM, 0,
                new ModelResourceLocation("minerustaddons:furnace", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModBlocks.ADMIN_FURNACE_ITEM, 0,
                new ModelResourceLocation(ModBlocks.ADMIN_FURNACE.getRegistryName(), "inventory"));
    }
}
