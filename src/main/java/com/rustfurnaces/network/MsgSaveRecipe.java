package com.rustfurnaces.network;

import com.rustfurnaces.ContainerAdminConfig;
import com.rustfurnaces.RecipeStore;
import com.rustfurnaces.SmeltRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Кнопка SAVE в ADMIN печи. Предметы берутся из слотов на сервере. */
public class MsgSaveRecipe implements IMessage {
    public int ticks;

    public MsgSaveRecipe() {
    }

    public MsgSaveRecipe(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ticks = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(ticks);
    }

    public static class Handler implements IMessageHandler<MsgSaveRecipe, IMessage> {
        @Override
        public IMessage onMessage(final MsgSaveRecipe message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (!(player.openContainer instanceof ContainerAdminConfig) || !RecipeStore.isAdmin(player)) return;
                    ContainerAdminConfig c = (ContainerAdminConfig) player.openContainer;
                    ItemStack in = c.config.getStackInSlot(0);
                    ItemStack fuel = c.config.getStackInSlot(1);
                    List<ItemStack> outs = new ArrayList<ItemStack>();
                    for (int j = 2; j < 5; j++) {
                        ItemStack s = c.config.getStackInSlot(j);
                        if (!s.isEmpty()) outs.add(s);
                    }
                    if (in.isEmpty()) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "Положи ресурс для плавки в слот INPUT."));
                        return;
                    }
                    if (outs.isEmpty()) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "Положи хотя бы один результат в слоты OUTPUT."));
                        return;
                    }
                    int ticks = Math.max(1, Math.min(30000, message.ticks));
                    SmeltRecipe r = new SmeltRecipe(UUID.randomUUID().toString(), in, fuel, ticks, outs);
                    RecipeStore.addOrReplace(r);
                    player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Сейв сохранён: "
                            + in.getCount() + "x " + in.getDisplayName()
                            + (fuel.isEmpty() ? " (без топлива)" : " + " + fuel.getCount() + "x " + fuel.getDisplayName())
                            + ", " + (ticks / 20.0F) + " сек. Применено ко всем печам."));
                }
            });
            return null;
        }
    }
}
