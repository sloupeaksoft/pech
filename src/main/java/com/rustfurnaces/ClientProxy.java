package com.rustfurnaces;

import com.rustfurnaces.client.GuiAdminConfig;
import com.rustfurnaces.client.GuiRustFurnace;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy {
    @Override
    public Object getClientGui(int id, EntityPlayer player, World world, BlockPos pos) {
        if (id == GuiHandler.FURNACE) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileRustFurnace) {
                boolean admin = world.getBlockState(pos).getBlock() == ModBlocks.ADMIN_FURNACE;
                return new GuiRustFurnace(new ContainerRustFurnace(player.inventory, (TileRustFurnace) te), admin);
            }
        } else if (id == GuiHandler.ADMIN_CONFIG) {
            return new GuiAdminConfig(new ContainerAdminConfig(player.inventory, pos));
        }
        return null;
    }

    @Override
    public void onRecipeSync(final NBTTagCompound tag) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                RecipeStore.setClient(RecipeStore.readList(tag));
            }
        });
    }
}
