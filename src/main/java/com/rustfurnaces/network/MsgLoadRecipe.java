package com.rustfurnaces.network;

import com.rustfurnaces.ContainerAdminConfig;
import com.rustfurnaces.RecipeStore;
import com.rustfurnaces.SmeltRecipe;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Кнопка EDIT: положить предметы сейва в слоты. */
public class MsgLoadRecipe implements IMessage {
    public String id = "";

    public MsgLoadRecipe() {
    }

    public MsgLoadRecipe(String id) {
        this.id = id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        id = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, id);
    }

    public static class Handler implements IMessageHandler<MsgLoadRecipe, IMessage> {
        @Override
        public IMessage onMessage(final MsgLoadRecipe message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (!(player.openContainer instanceof ContainerAdminConfig) || !RecipeStore.isAdmin(player)) return;
                    SmeltRecipe r = RecipeStore.byId(message.id);
                    if (r != null) {
                        ((ContainerAdminConfig) player.openContainer).loadRecipe(player, r);
                    }
                }
            });
            return null;
        }
    }
}
