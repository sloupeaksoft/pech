package com.rustfurnaces;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.items.ItemStackHandler;

/** Печь: слот 0 = FUEL, 1-2 = INPUT, 3-5 = OUTPUT. Рецепты берутся из RecipeStore (общие для всех печей). */
public class TileRustFurnace extends TileEntity implements ITickable {
    public static final int FUEL = 0;
    public static final int IN0 = 1;
    public static final int OUT0 = 3;
    public static final int SLOTS = 6;

    public final ItemStackHandler inv = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            TileRustFurnace.this.markDirty();
        }
    };

    public boolean on;
    public final int[] progress = new int[2];
    public final int[] maxTime = new int[2];

    public void toggle() {
        on = !on;
        if (!on) {
            progress[0] = 0;
            progress[1] = 0;
            maxTime[0] = 0;
            maxTime[1] = 0;
        }
        markDirty();
    }

    @Override
    public void update() {
        if (world == null || world.isRemote || !on) return;

        List<SmeltRecipe> recipes = RecipeStore.get(false);
        boolean active = false;

        for (int i = 0; i < 2; i++) {
            ItemStack in = inv.getStackInSlot(IN0 + i);
            ItemStack fuel = inv.getStackInSlot(FUEL);
            SmeltRecipe r = RecipeStore.find(recipes, in, fuel);
            boolean ok = r != null
                    && in.getCount() >= r.input.getCount()
                    && (r.fuel.isEmpty() || fuel.getCount() >= r.fuel.getCount())
                    && insertOutputs(r.outputs, true);
            if (ok) {
                active = true;
                maxTime[i] = r.time;
                progress[i]++;
                if (progress[i] >= r.time) {
                    progress[i] = 0;
                    inv.extractItem(IN0 + i, r.input.getCount(), false);
                    if (!r.fuel.isEmpty()) {
                        inv.extractItem(FUEL, r.fuel.getCount(), false);
                    }
                    insertOutputs(r.outputs, false);
                }
            } else {
                progress[i] = 0;
                maxTime[i] = 0;
            }
        }

        if (!active) {
            on = false;
            markDirty();
        }
    }

    /** simulate=true: проверить, влезет ли результат в OUTPUT; simulate=false: положить. */
    private boolean insertOutputs(List<ItemStack> outputs, boolean simulate) {
        ItemStack[] slots = new ItemStack[3];
        for (int j = 0; j < 3; j++) {
            slots[j] = inv.getStackInSlot(OUT0 + j).copy();
        }
        for (ItemStack out : outputs) {
            ItemStack rem = out.copy();
            for (int pass = 0; pass < 2 && !rem.isEmpty(); pass++) {
                for (int j = 0; j < 3 && !rem.isEmpty(); j++) {
                    ItemStack s = slots[j];
                    if (pass == 0) {
                        if (!s.isEmpty() && ItemStack.areItemsEqual(s, rem) && ItemStack.areItemStackTagsEqual(s, rem)) {
                            int space = Math.min(s.getMaxStackSize(), 64) - s.getCount();
                            int move = Math.min(space, rem.getCount());
                            if (move > 0) {
                                s.grow(move);
                                rem.shrink(move);
                            }
                        }
                    } else if (s.isEmpty()) {
                        int move = Math.min(rem.getCount(), rem.getMaxStackSize());
                        ItemStack n = rem.copy();
                        n.setCount(move);
                        slots[j] = n;
                        rem.shrink(move);
                    }
                }
            }
            if (!rem.isEmpty()) return false;
        }
        if (!simulate) {
            for (int j = 0; j < 3; j++) {
                inv.setStackInSlot(OUT0 + j, slots[j]);
            }
        }
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        inv.deserializeNBT(compound.getCompoundTag("Inv"));
        on = compound.getBoolean("On");
        int[] p = compound.getIntArray("Progress");
        if (p.length == 2) {
            progress[0] = p[0];
            progress[1] = p[1];
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Inv", inv.serializeNBT());
        compound.setBoolean("On", on);
        compound.setIntArray("Progress", new int[]{progress[0], progress[1]});
        return compound;
    }
}
