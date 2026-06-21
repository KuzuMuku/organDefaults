package cn.kuzuanpa.organdefaults;

import cn.kuzuanpa.organapi.api.extension.OrganBootstrapApi;
import cn.kuzuanpa.organdefaults.common.DefaultLoadoutLoader;
import cn.kuzuanpa.organdefaults.common.DefaultOrganBootstrapper;
import cn.kuzuanpa.organdefaults.common.registry.OrganDefaultsItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(OrganDefaultsMod.MOD_ID)
public class OrganDefaultsMod {
    public static final String MOD_ID = "organdefaults";

    public OrganDefaultsMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        OrganDefaultsItems.ITEMS.register(modBus);

        OrganBootstrapApi.register(new DefaultOrganBootstrapper());
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(DefaultLoadoutLoader.INSTANCE);
    }
}
