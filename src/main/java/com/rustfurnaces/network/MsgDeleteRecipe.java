package com.rustfurnaces.network;

import com.rustfurnaces.ContainerAdminConfig;
import com.rustfurnaces.RecipeStore;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Кнопка DELETE. */
public class MsgDeleteRecipe implements IMessage {
    public String id = "";

    public MsgDeleteRecipe() {
    }

    public MsgDeleteRecipe(String id) {
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

    public static class Handler implements IMessageHandler<MsgDeleteRecipe, IMessage> {
        @Override
        public IMessage onMessage(final MsgDeleteRecipe message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (!(player.openContainer instanceof ContainerAdminConfig) || !RecipeStore.isAdmin(player)) return;
                    if (RecipeStore.remove(message.id)) {
                        player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Сейв плавки удалён."));
                    }
                }
            });
            return null;
        }
    }
}
