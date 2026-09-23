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

package cn.kuzuanpa.organdefaults;

import cn.kuzuanpa.organapi.api.extension.OrganBootstrapApi;
import cn.kuzuanpa.organapi.common.data.pack.OrganPackTemplateExtractor;
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
        OrganPackTemplateExtractor.extractPackIfAbsent(MOD_ID, "organdefaults_content");
        OrganDefaultsItems.ITEMS.register(modBus);

        OrganBootstrapApi.register(new DefaultOrganBootstrapper());
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(DefaultLoadoutLoader.INSTANCE);
    }
}
