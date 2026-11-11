package com.rustfurnaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Настройка ADMIN печи. Слоты: 0 = INPUT, 1 = FUEL, 2-4 = OUTPUT.
 * Количество предметов в слоте = сколько нужно / сколько получится.
 * При закрытии окна предметы возвращаются игроку.
 */
public class ContainerAdminConfig extends Container {
    public final InventoryBasic config = new InventoryBasic("rustfurnaces_config", false, 5);
    public final BlockPos pos;
    private final World world;

    public ContainerAdminConfig(InventoryPlayer playerInv, BlockPos pos) {
        this.pos = pos;
        this.world = playerInv.player.world;
        addSlotToContainer(new Slot(config, 0, 92, 72));
        addSlotToContainer(new Slot(config, 1, 92, 36));
        for (int j = 0; j < 3; j++) {
            addSlotToContainer(new Slot(config, 2 + j, 64 + 28 * j, 112));
        }
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                addSlotToContainer(new Slot(playerInv, c + r * 9 + 9, ContainerRustFurnace.INV_X + c * 18, ContainerRustFurnace.INV_Y + r * 18));
            }
        }
        for (int c = 0; c < 9; c++) {
            addSlotToContainer(new Slot(playerInv, c, ContainerRustFurnace.INV_X + c * 18, ContainerRustFurnace.INV_Y + 58));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!world.isRemote) {
            clearContainer(player, world, config);
        }
    }

    /** EDIT: вернуть текущие предметы игроку и положить в слоты предметы выбранного сейва. */
    public void loadRecipe(EntityPlayer player, SmeltRecipe r) {
        clearContainer(player, world, config);
        config.setInventorySlotContents(0, r.input.copy());
        config.setInventorySlotContents(1, r.fuel.copy());
        for (int j = 0; j < 3; j++) {
            config.setInventorySlotContents(2 + j, j < r.outputs.size() ? r.outputs.get(j).copy() : ItemStack.EMPTY);
        }
        detectAndSendChanges();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();
            if (index < 5) {
                if (!mergeItemStack(stack, 5, 41, true)) return ItemStack.EMPTY;
            } else {
                if (!mergeItemStack(stack, 0, 5, false)) return ItemStack.EMPTY;
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
}
