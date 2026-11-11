package com.rustfurnaces.network;

import com.rustfurnaces.ContainerRustFurnace;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Кнопка TURN ON / TURN OFF. */
public class MsgToggle implements IMessage {
    public MsgToggle() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MsgToggle, IMessage> {
        @Override
        public IMessage onMessage(MsgToggle message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (player.openContainer instanceof ContainerRustFurnace) {
                        ((ContainerRustFurnace) player.openContainer).te.toggle();
                    }
                }
            });
            return null;
        }
    }
}
