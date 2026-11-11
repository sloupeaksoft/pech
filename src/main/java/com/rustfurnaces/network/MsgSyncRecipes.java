package com.rustfurnaces.network;

import com.rustfurnaces.RustFurnaces;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Сервер -> клиент: полный список сейвов плавки. */
public class MsgSyncRecipes implements IMessage {
    public NBTTagCompound tag = new NBTTagCompound();

    public MsgSyncRecipes() {
    }

    public MsgSyncRecipes(NBTTagCompound tag) {
        this.tag = tag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound t = ByteBufUtils.readTag(buf);
        tag = t == null ? new NBTTagCompound() : t;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, tag);
    }

    public static class Handler implements IMessageHandler<MsgSyncRecipes, IMessage> {
        @Override
        public IMessage onMessage(MsgSyncRecipes message, MessageContext ctx) {
            RustFurnaces.proxy.onRecipeSync(message.tag);
            return null;
        }
    }
}
