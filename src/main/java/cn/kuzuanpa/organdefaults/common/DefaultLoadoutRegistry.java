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

package cn.kuzuanpa.organdefaults.common;

import java.util.List;

public final class DefaultLoadoutRegistry {
    private static volatile List<DefaultLoadoutDefinition> LOADOUTS = List.of();

    private DefaultLoadoutRegistry() {
    }

    public static List<DefaultLoadoutDefinition> getLoadouts() {
        return LOADOUTS;
    }

    public static void setLoadouts(List<DefaultLoadoutDefinition> loadouts) {
        LOADOUTS = List.copyOf(loadouts);
    }
}
