package com.rustfurnaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Один сейв плавки: что плавим (и сколько), топливо (и сколько), время в тиках, до 3 результатов. */
public class SmeltRecipe {
    public final String id;
    public final ItemStack input;
    public final ItemStack fuel;
    public final int time;
    public final List<ItemStack> outputs;

    public SmeltRecipe(String id, ItemStack input, ItemStack fuel, int time, List<ItemStack> outputs) {
        this.id = id;
        this.input = input.copy();
        this.fuel = fuel.isEmpty() ? ItemStack.EMPTY : fuel.copy();
        this.time = Math.max(1, time);
        List<ItemStack> outs = new ArrayList<ItemStack>();
        for (ItemStack s : outputs) {
            if (!s.isEmpty()) outs.add(s.copy());
        }
        this.outputs = Collections.unmodifiableList(outs);
    }

    public static boolean same(ItemStack a, ItemStack b) {
        return !a.isEmpty() && !b.isEmpty() && a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata();
    }

    public boolean sameKey(SmeltRecipe o) {
        if (!same(input, o.input)) return false;
        if (fuel.isEmpty()) return o.fuel.isEmpty();
        return same(fuel, o.fuel);
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound t = new NBTTagCompound();
        t.setString("Id", id);
        t.setTag("In", input.writeToNBT(new NBTTagCompound()));
        if (!fuel.isEmpty()) {
            t.setTag("Fuel", fuel.writeToNBT(new NBTTagCompound()));
        }
        t.setInteger("Time", time);
        NBTTagList outs = new NBTTagList();
        for (ItemStack s : outputs) {
            outs.appendTag(s.writeToNBT(new NBTTagCompound()));
        }
        t.setTag("Outs", outs);
        return t;
    }

    public static SmeltRecipe fromNBT(NBTTagCompound t) {
        ItemStack in = new ItemStack(t.getCompoundTag("In"));
        if (in.isEmpty()) return null;
        ItemStack fuel = t.hasKey("Fuel") ? new ItemStack(t.getCompoundTag("Fuel")) : ItemStack.EMPTY;
        List<ItemStack> outs = new ArrayList<ItemStack>();
        NBTTagList list = t.getTagList("Outs", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack s = new ItemStack(list.getCompoundTagAt(i));
            if (!s.isEmpty()) outs.add(s);
        }
        if (outs.isEmpty()) return null;
        return new SmeltRecipe(t.getString("Id"), in, fuel, t.getInteger("Time"), outs);
    }
}
