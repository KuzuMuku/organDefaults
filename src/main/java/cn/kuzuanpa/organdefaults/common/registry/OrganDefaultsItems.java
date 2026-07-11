/*
 * This class was created by <kuzuanpa>. It is distributed as
 * part of the organDefaults Mod. Get the Source Code in github:
 * https://github.com/KuzuMuku/organDefaults
 *
 * organDefaults is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.

 * organDefaults is Open Source and distributed under the
 * AGPLv3 License: https://www.gnu.org/licenses/agpl-3.0.txt
 *
 */

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
    public static final RegistryObject<Item> BASELINE_ORGAN = register("baseline_organ");
    public static final RegistryObject<Item> COMMON_HEART = register("common_heart");
    public static final RegistryObject<Item> STRONG_HEART = register("strong_heart");
    public static final RegistryObject<Item> ROTTEN_HEART = register("rotten_heart");
    public static final RegistryObject<Item> COMMON_LUNGS = register("common_lungs");
    public static final RegistryObject<Item> AQUATIC_LUNGS = register("aquatic_lungs");
    public static final RegistryObject<Item> FISH_GILLS = register("fish_gills");
    public static final RegistryObject<Item> ROTTEN_LUNGS = register("rotten_lungs");
    public static final RegistryObject<Item> COMMON_LIVER = register("common_liver");
    public static final RegistryObject<Item> TOXIC_LIVER = register("toxic_liver");
    public static final RegistryObject<Item> ROTTEN_LIVER = register("rotten_liver");
    public static final RegistryObject<Item> COMMON_KIDNEY_PAIR = register("common_kidney_pair");
    public static final RegistryObject<Item> STRONG_KIDNEY_PAIR = register("strong_kidney_pair");
    public static final RegistryObject<Item> ROTTEN_KIDNEY_PAIR = register("rotten_kidney_pair");
    public static final RegistryObject<Item> COMMON_DIGESTIVE_TRACT = register("common_digestive_tract");
    public static final RegistryObject<Item> HERBIVORE_DIGESTIVE_TRACT = register("herbivore_digestive_tract");
    public static final RegistryObject<Item> PREDATOR_DIGESTIVE_TRACT = register("predator_digestive_tract");
    public static final RegistryObject<Item> ROTTEN_DIGESTIVE_TRACT = register("rotten_digestive_tract");
    public static final RegistryObject<Item> COMMON_BRAIN = register("common_brain");
    public static final RegistryObject<Item> KEEN_BRAIN = register("keen_brain");
    public static final RegistryObject<Item> ROTTEN_BRAIN = register("rotten_brain");
    public static final RegistryObject<Item> COMMON_SPINE = register("common_spine");
    public static final RegistryObject<Item> KEEN_SPINE = register("keen_spine");
    public static final RegistryObject<Item> ROTTEN_SPINE = register("rotten_spine");
    public static final RegistryObject<Item> MUSCLE_BUNDLE = register("muscle_bundle");
    public static final RegistryObject<Item> STRONG_MUSCLE_BUNDLE = register("strong_muscle_bundle");
    public static final RegistryObject<Item> TITAN_MUSCLE_BUNDLE = register("titan_muscle_bundle");
    public static final RegistryObject<Item> AGILE_MUSCLE_BUNDLE = register("agile_muscle_bundle");
    public static final RegistryObject<Item> ROTTEN_MUSCLE_BUNDLE = register("rotten_muscle_bundle");
    public static final RegistryObject<Item> RIB_CAGE = register("rib_cage");
    public static final RegistryObject<Item> REINFORCED_RIB_CAGE = register("reinforced_rib_cage");
    public static final RegistryObject<Item> COMMON_SKULL = register("common_skull");
    public static final RegistryObject<Item> LIGHT_SKULL = register("light_skull");
    public static final RegistryObject<Item> CARAPACE_SHELL = register("carapace_shell");
    public static final RegistryObject<Item> UNDEAD_CORE = register("undead_core");
    public static final RegistryObject<Item> GOLEM_CORE = register("golem_core");
    public static final RegistryObject<Item> VOID_CORE = register("void_core");
    public static final RegistryObject<Item> BLAZE_CORE = register("blaze_core");
    public static final RegistryObject<Item> SLIME_CORE = register("slime_core");
    public static final RegistryObject<Item> EXPLOSIVE_CORE = register("explosive_core");
    public static final RegistryObject<Item> IRON_PLATING = register("iron_plating");
    public static final RegistryObject<Item> SHULKER_BOLT_GLAND = register("shulker_bolt_gland");
    public static final RegistryObject<Item> GUARDIAN_LENS = register("guardian_lens");
    public static final RegistryObject<Item> END_CRYSTAL_RESONATOR = register("end_crystal_resonator");
    public static final RegistryObject<Item> DRAGON_BREATH_GLAND = register("dragon_breath_gland");

    private OrganDefaultsItems() {
    }

    private static RegistryObject<Item> register(String name) {
        return ITEMS.register(name,
                () -> new OrganItem(new Item.Properties().stacksTo(1), ResourceLocation.fromNamespaceAndPath(OrganDefaultsMod.MOD_ID, name)));
    }
}
