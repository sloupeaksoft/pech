package com.rustfurnaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
    public static final int FURNACE = 0;
    public static final int ADMIN_CONFIG = 1;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (id == FURNACE) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileRustFurnace) {
                return new ContainerRustFurnace(player.inventory, (TileRustFurnace) te);
            }
        } else if (id == ADMIN_CONFIG) {
            return new ContainerAdminConfig(player.inventory, pos);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return RustFurnaces.proxy.getClientGui(id, player, world, new BlockPos(x, y, z));
    }
}
