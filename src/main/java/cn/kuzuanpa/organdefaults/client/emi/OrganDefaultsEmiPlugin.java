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

package cn.kuzuanpa.organdefaults.client.emi;

import cn.kuzuanpa.organdefaults.OrganDefaultsMod;
import cn.kuzuanpa.organdefaults.common.DefaultLoadoutRegistry;
import cn.kuzuanpa.organdefaults.common.registry.OrganDefaultsItems;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;

@EmiEntrypoint
public final class OrganDefaultsEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory DEFAULT_LOADOUT_CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(OrganDefaultsMod.MOD_ID, "default_loadouts"),
            EmiStack.of(OrganDefaultsItems.BASELINE_ORGAN.get()));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(DEFAULT_LOADOUT_CATEGORY);
        registry.addWorkstation(DEFAULT_LOADOUT_CATEGORY, EmiStack.of(OrganDefaultsItems.BASELINE_ORGAN.get()));
        registry.addDeferredRecipes(consumer -> DefaultLoadoutRegistry.getLoadouts().stream()
                .flatMap(loadout -> OrganDefaultsEmiRecipe.expand(loadout).stream())
                .forEach(consumer));
    }
}
