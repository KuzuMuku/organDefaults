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

import cn.kuzuanpa.organapi.api.extension.OrganHolderBootstrapper;
import cn.kuzuanpa.organapi.common.body.BodyPlanResolver;
import cn.kuzuanpa.organapi.common.capability.IOrganHolder;
import cn.kuzuanpa.organapi.core.model.OrganId;
import cn.kuzuanpa.organapi.core.model.ResolvedBodyPlan;
import cn.kuzuanpa.organapi.platform.OrganIds;
import cn.kuzuanpa.organeffects.api.OrganEffectsPointApi;
import com.mojang.logging.LogUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

public class DefaultOrganBootstrapper implements OrganHolderBootstrapper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String INITIALIZED_KEY = "organdefaults.initialized";
    private static final Set<UUID> INITIALIZING = ConcurrentHashMap.newKeySet();

    @Override
    public void bootstrap(Entity entity, IOrganHolder holder) {
        if (!(entity instanceof LivingEntity living) || living.level().isClientSide()) {
            return;
        }
        if (isInitialized(entity) || INITIALIZING.contains(entity.getUUID())) {
            return;
        }
        if (hasAnyOrgan(living, holder)) {
            markInitialized(entity);
            return;
        }
        List<DefaultLoadoutDefinition> matchingLoadouts = DefaultLoadoutRegistry.getLoadouts().stream()
                .filter(loadout -> loadout.matches(entity))
                .sorted(Comparator.comparingInt(DefaultLoadoutDefinition::priority))
                .toList();
        if (matchingLoadouts.isEmpty()) {
            return;
        }

        INITIALIZING.add(entity.getUUID());
        try {
            for (DefaultLoadoutDefinition loadout : matchingLoadouts) {
                applyPlacements(living, holder, loadout);
                applyPointAdjustments(living, loadout);
            }
            markInitialized(entity);
            OrganEffectsPointApi.recompute(living);
        } finally {
            INITIALIZING.remove(entity.getUUID());
        }
    }

    private static void applyPlacements(LivingEntity entity, IOrganHolder holder, DefaultLoadoutDefinition loadout) {
        for (Map.Entry<ResourceLocation, Map<Integer, List<DefaultLoadoutDefinition.OrganOption>>> bodyPartEntry : loadout.placements().entrySet()) {
            ResourceLocation bodyPartId = bodyPartEntry.getKey();
            for (Map.Entry<Integer, List<DefaultLoadoutDefinition.OrganOption>> slotEntry : bodyPartEntry.getValue().entrySet()) {
                int slotIndex = slotEntry.getKey();
                DefaultLoadoutDefinition.OrganOption selected = selectOption(entity, slotEntry.getValue());
                if (selected == null) {
                    continue;
                }
                Item item = ForgeRegistries.ITEMS.getValue(selected.itemId());
                if (item == null) {
                    LOGGER.warn("Failed to resolve default organ item {} for {}", selected.itemId(), loadout.id());
                    continue;
                }
                ItemStack stack = new ItemStack(item);
                var result = holder.trySetOrgan(bodyPartId, slotIndex, stack);
                if (!result.success()) {
                    LOGGER.warn("Failed to install default organ {} for {} at {}#{}: {}",
                            selected.itemId(), loadout.id(), bodyPartId, slotIndex, result.message().getString());
                }
            }
        }
    }

    private static DefaultLoadoutDefinition.OrganOption selectOption(
            LivingEntity entity, List<DefaultLoadoutDefinition.OrganOption> options
    ) {
        List<DefaultLoadoutDefinition.OrganOption> eligible = options.stream()
                .filter(option -> option.minMaxHealth() == null || entity.getMaxHealth() >= option.minMaxHealth())
                .toList();
        if (eligible.isEmpty()) {
            return null;
        }
        if (eligible.size() == 1) {
            return eligible.get(0);
        }
        int totalWeight = eligible.stream().mapToInt(DefaultLoadoutDefinition.OrganOption::weight).sum();
        if (totalWeight <= 0) {
            return null;
        }
        int roll = entity.getRandom().nextInt(totalWeight);
        int current = 0;
        for (DefaultLoadoutDefinition.OrganOption option : eligible) {
            current += option.weight();
            if (roll < current) {
                return option;
            }
        }
        return eligible.get(eligible.size() - 1);
    }

    private static void applyPointAdjustments(LivingEntity entity, DefaultLoadoutDefinition loadout) {
        if (loadout.pointAdjustments().isEmpty()) {
            return;
        }
        Map<String, Map<String, Long>> grouped = new LinkedHashMap<>();
        for (DefaultLoadoutDefinition.PointAdjustment adjustment : loadout.pointAdjustments()) {
            String source = adjustment.sourceTag() != null ? adjustment.sourceTag() : loadout.sourceTag();
            grouped.computeIfAbsent(source, key -> new LinkedHashMap<>()).put(adjustment.pointKey(), adjustment.amount());
        }
        for (Map.Entry<String, Map<String, Long>> entry : grouped.entrySet()) {
            OrganEffectsPointApi.replaceSourcePoints(entity, entry.getKey(), entry.getValue());
        }
    }

    private static boolean hasAnyOrgan(LivingEntity entity, IOrganHolder holder) {
        ResolvedBodyPlan bodyPlan = BodyPlanResolver.resolve(entity);
        for (OrganId bodyPartId : bodyPlan.getOrderedBodyPartIds()) {
            for (ItemStack stack : holder.getInstalledOrgans(OrganIds.toResourceLocation(bodyPartId))) {
                if (!stack.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isInitialized(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.getBoolean(INITIALIZED_KEY);
    }

    private static void markInitialized(Entity entity) {
        entity.getPersistentData().putBoolean(INITIALIZED_KEY, true);
    }
}
