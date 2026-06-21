package cn.kuzuanpa.organdefaults.common.registry;

import cn.kuzuanpa.organdefaults.OrganDefaultsMod;
import cn.kuzuanpa.organapi.common.item.OrganItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OrganDefaultsItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, OrganDefaultsMod.MOD_ID);
    public static final RegistryObject<Item> BASELINE_ORGAN = ITEMS.register("baseline_organ",
            () -> new OrganItem(new Item.Properties().stacksTo(1), ResourceLocation.fromNamespaceAndPath(OrganDefaultsMod.MOD_ID, "baseline_organ")));

    private OrganDefaultsItems() {
    }
}
