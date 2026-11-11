package com.rustfurnaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@Mod.EventBusSubscriber(modid = RustFurnaces.MODID)
public class CommonEvents {

    /** Shift + ЛКМ по ADMIN печи открывает настройки плавки. */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        EntityPlayer player = event.getEntityPlayer();
        if (!player.isSneaking()) return;
        BlockPos pos = event.getPos();
        if (world.getBlockState(pos).getBlock() != ModBlocks.ADMIN_FURNACE) return;

        event.setCanceled(true);
        if (RecipeStore.isAdmin(player)) {
            player.openGui(RustFurnaces.instance, GuiHandler.ADMIN_CONFIG, world, pos.getX(), pos.getY(), pos.getZ());
        } else {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Настраивать ADMIN печь может только администратор (OP)."));
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            RecipeStore.syncTo((EntityPlayerMP) event.player);
        }
    }
}
