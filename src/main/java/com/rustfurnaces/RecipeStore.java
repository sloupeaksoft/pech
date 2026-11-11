package com.rustfurnaces;

import com.rustfurnaces.network.MsgSyncRecipes;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Все сейвы плавки. Серверный список хранится в папке мира (rustfurnaces_recipes.dat)
 * и сразу рассылается всем игрокам. Печи читают его каждый тик, поэтому изменения
 * применяются ко всем печам мгновенно.
 */
public final class RecipeStore {
    private static final Logger LOG = LogManager.getLogger(RustFurnaces.MODID);
    private static volatile List<SmeltRecipe> server = Collections.emptyList();
    private static volatile List<SmeltRecipe> client = Collections.emptyList();
    private static File file;

    private RecipeStore() {
    }

    public static List<SmeltRecipe> get(boolean remote) {
        return remote ? client : server;
    }

    public static void setClient(List<SmeltRecipe> list) {
        client = Collections.unmodifiableList(new ArrayList<SmeltRecipe>(list));
    }

    /** OP (уровень 2+) на сервере или владелец одиночного мира. */
    public static boolean isAdmin(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) return false;
        EntityPlayerMP mp = (EntityPlayerMP) player;
        if (mp.canUseCommand(2, "rustfurnaces")) return true;
        MinecraftServer srv = mp.getServer();
        return srv != null && srv.isSinglePlayer() && mp.getName().equals(srv.getServerOwner());
    }

    public static void loadServer(MinecraftServer srv) {
        File dir = srv.getEntityWorld().getSaveHandler().getWorldDirectory();
        file = new File(dir, "rustfurnaces_recipes.dat");
        List<SmeltRecipe> list = new ArrayList<SmeltRecipe>();
        boolean exists = file.exists();
        if (exists) {
            try {
                NBTTagCompound tag = CompressedStreamTools.read(file);
                if (tag != null) list = readList(tag);
            } catch (Exception e) {
                LOG.error("Cannot read " + file, e);
            }
        } else {
            list = defaults();
        }
        server = Collections.unmodifiableList(list);
        if (!exists) save();
        LOG.info("Loaded smelting saves: " + list.size());
    }

    public static void unloadServer() {
        server = Collections.emptyList();
        file = null;
    }

    public static synchronized void addOrReplace(SmeltRecipe recipe) {
        List<SmeltRecipe> list = new ArrayList<SmeltRecipe>(server);
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).sameKey(recipe)) list.remove(i);
        }
        list.add(recipe);
        server = Collections.unmodifiableList(list);
        save();
        syncAll();
    }

    public static synchronized boolean remove(String id) {
        List<SmeltRecipe> list = new ArrayList<SmeltRecipe>(server);
        boolean removed = false;
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).id.equals(id)) {
                list.remove(i);
                removed = true;
            }
        }
        if (removed) {
            server = Collections.unmodifiableList(list);
            save();
            syncAll();
        }
        return removed;
    }

    public static SmeltRecipe byId(String id) {
        for (SmeltRecipe r : server) {
            if (r.id.equals(id)) return r;
        }
        return null;
    }

    public static SmeltRecipe find(List<SmeltRecipe> list, ItemStack in, ItemStack fuel) {
        if (in.isEmpty()) return null;
        SmeltRecipe noFuel = null;
        for (SmeltRecipe r : list) {
            if (!SmeltRecipe.same(r.input, in)) continue;
            if (r.fuel.isEmpty()) {
                if (noFuel == null) noFuel = r;
            } else if (SmeltRecipe.same(r.fuel, fuel)) {
                return r;
            }
        }
        return noFuel;
    }

    public static boolean isFuel(ItemStack stack, boolean remote) {
        for (SmeltRecipe r : get(remote)) {
            if (SmeltRecipe.same(r.fuel, stack)) return true;
        }
        return false;
    }

    private static void save() {
        if (file == null) return;
        try {
            CompressedStreamTools.safeWrite(writeList(server), file);
        } catch (Exception e) {
            LOG.error("Cannot save " + file, e);
        }
    }

    public static NBTTagCompound writeList(List<SmeltRecipe> list) {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList recipes = new NBTTagList();
        for (SmeltRecipe r : list) {
            recipes.appendTag(r.toNBT());
        }
        tag.setTag("Recipes", recipes);
        return tag;
    }

    public static List<SmeltRecipe> readList(NBTTagCompound tag) {
        List<SmeltRecipe> list = new ArrayList<SmeltRecipe>();
        NBTTagList recipes = tag.getTagList("Recipes", 10);
        for (int i = 0; i < recipes.tagCount(); i++) {
            SmeltRecipe r = SmeltRecipe.fromNBT(recipes.getCompoundTagAt(i));
            if (r != null) list.add(r);
        }
        return list;
    }

    public static void syncAll() {
        RustFurnaces.NET.sendToAll(new MsgSyncRecipes(writeList(server)));
    }

    public static void syncTo(EntityPlayerMP player) {
        RustFurnaces.NET.sendTo(new MsgSyncRecipes(writeList(server)), player);
    }

    /** Стартовые примеры (если предметы MineRust найдены). Их можно удалить в ADMIN печи. */
    private static List<SmeltRecipe> defaults() {
        List<SmeltRecipe> list = new ArrayList<SmeltRecipe>();
        Item wood = item("piecewood");
        Item metalOre = item("metalore");
        Item metalFrag = item("metalfragments");
        Item sulfurOre = item("sulfurore");
        Item sulfur = item("sulfur");
        if (wood != null && metalOre != null && metalFrag != null) {
            list.add(new SmeltRecipe(UUID.randomUUID().toString(), new ItemStack(metalOre), new ItemStack(wood), 100,
                    Collections.singletonList(new ItemStack(metalFrag))));
        }
        if (wood != null && sulfurOre != null && sulfur != null) {
            list.add(new SmeltRecipe(UUID.randomUUID().toString(), new ItemStack(sulfurOre), new ItemStack(wood), 100,
                    Collections.singletonList(new ItemStack(sulfur))));
        }
        return list;
    }

    private static Item item(String name) {
        Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minerustaddons", name));
        return (it == null || it == Items.AIR) ? null : it;
    }
}
