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
