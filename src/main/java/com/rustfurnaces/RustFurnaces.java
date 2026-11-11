package com.rustfurnaces;

import com.rustfurnaces.network.MsgDeleteRecipe;
import com.rustfurnaces.network.MsgLoadRecipe;
import com.rustfurnaces.network.MsgSaveRecipe;
import com.rustfurnaces.network.MsgSyncRecipes;
import com.rustfurnaces.network.MsgToggle;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = RustFurnaces.MODID,
        name = RustFurnaces.NAME,
        version = RustFurnaces.VERSION,
        dependencies = "required-after:minerustaddons",
        acceptedMinecraftVersions = "[1.12.2]"
)
public class RustFurnaces {
    public static final String MODID = "rustfurnaces";
    public static final String NAME = "Rust Furnaces (MineRust addon)";
    public static final String VERSION = "1.0";

    @Mod.Instance(MODID)
    public static RustFurnaces instance;

    @SidedProxy(clientSide = "com.rustfurnaces.ClientProxy", serverSide = "com.rustfurnaces.CommonProxy")
    public static CommonProxy proxy;

    public static SimpleNetworkWrapper NET;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NET = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        int id = 0;
        NET.registerMessage(MsgToggle.Handler.class, MsgToggle.class, id++, Side.SERVER);
        NET.registerMessage(MsgSaveRecipe.Handler.class, MsgSaveRecipe.class, id++, Side.SERVER);
        NET.registerMessage(MsgDeleteRecipe.Handler.class, MsgDeleteRecipe.class, id++, Side.SERVER);
        NET.registerMessage(MsgLoadRecipe.Handler.class, MsgLoadRecipe.class, id++, Side.SERVER);
        NET.registerMessage(MsgSyncRecipes.Handler.class, MsgSyncRecipes.class, id++, Side.CLIENT);
        GameRegistry.registerTileEntity(TileRustFurnace.class, new ResourceLocation(MODID, "rust_furnace"));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        RecipeStore.loadServer(event.getServer());
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        RecipeStore.unloadServer();
    }
}
