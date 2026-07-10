package cn.kuzuanpa.organdefaults.client;

import cn.kuzuanpa.organdefaults.OrganDefaultsMod;
import cn.kuzuanpa.organdefaults.common.DefaultLoadoutLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OrganDefaultsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class OrganDefaultsClientEvents {
    private OrganDefaultsClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(DefaultLoadoutLoader.INSTANCE);
    }
}
