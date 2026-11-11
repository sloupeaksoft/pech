package com.rustfurnaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerRustFurnace extends Container {
    public static final int INV_X = 19;
    public static final int INV_Y = 162;

    public final TileRustFurnace te;
    /** 0 = включена, 1-2 = прогресс INPUT слотов, 3-4 = полное время плавки. */
    public final int[] fields = new int[5];
    private final int[] last = {-1, -1, -1, -1, -1};

    public ContainerRustFurnace(InventoryPlayer playerInv, TileRustFurnace te) {
        this.te = te;
        addSlotToContainer(new SlotItemHandler(te.inv, TileRustFurnace.FUEL, 92, 36));
        addSlotToContainer(new SlotItemHandler(te.inv, TileRustFurnace.IN0, 78, 72));
        addSlotToContainer(new SlotItemHandler(te.inv, TileRustFurnace.IN0 + 1, 106, 72));
        for (int j = 0; j < 3; j++) {
            addSlotToContainer(new SlotOutput(te.inv, TileRustFurnace.OUT0 + j, 64 + 28 * j, 112));
        }
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                addSlotToContainer(new Slot(playerInv, c + r * 9 + 9, INV_X + c * 18, INV_Y + r * 18));
            }
        }
        for (int c = 0; c < 9; c++) {
            addSlotToContainer(new Slot(playerInv, c, INV_X + c * 18, INV_Y + 58));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        BlockPos p = te.getPos();
        return player.world.getTileEntity(p) == te
                && player.getDistanceSq(p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D) <= 64.0D;
    }

    private int[] values() {
        return new int[]{te.on ? 1 : 0, te.progress[0], te.progress[1], te.maxTime[0], te.maxTime[1]};
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        int[] v = values();
        for (int i = 0; i < v.length; i++) {
            listener.sendWindowProperty(this, i, v[i]);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int[] v = values();
        for (int i = 0; i < v.length; i++) {
            if (v[i] != last[i]) {
                for (IContainerListener l : this.listeners) {
                    l.sendWindowProperty(this, i, v[i]);
                }
                last[i] = v[i];
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int data) {
        if (id >= 0 && id < fields.length) fields[id] = data;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();
            if (index < 6) {
                if (!mergeItemStack(stack, 6, 42, true)) return ItemStack.EMPTY;
            } else if (RecipeStore.isFuel(stack, player.world.isRemote)) {
                if (!mergeItemStack(stack, 0, 1, false) && !mergeItemStack(stack, 1, 3, false)) return ItemStack.EMPTY;
            } else {
                if (!mergeItemStack(stack, 1, 3, false)) return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return result;
    }

    /** Слот результата: класть нельзя, только забирать. */
    public static class SlotOutput extends SlotItemHandler {
        public SlotOutput(IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}
