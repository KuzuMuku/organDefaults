package cn.kuzuanpa.organdefaults.common;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public record DefaultLoadoutDefinition(
        ResourceLocation id,
        int priority,
        Set<ResourceLocation> entityTypes,
        Set<ResourceLocation> entityTags,
        Map<ResourceLocation, Map<Integer, List<OrganOption>>> placements,
        List<PointAdjustment> pointAdjustments
) {
    public DefaultLoadoutDefinition {
        entityTypes = Set.copyOf(entityTypes);
        entityTags = Set.copyOf(entityTags);
        placements = placements.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        slotEntry -> List.copyOf(slotEntry.getValue())
                ))
        ));
        pointAdjustments = List.copyOf(pointAdjustments);
    }

    public boolean matches(Entity entity) {
        ResourceLocation entityTypeId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityTypeId != null && entityTypes.contains(entityTypeId)) {
            return true;
        }
        for (ResourceLocation tagId : entityTags) {
            TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, tagId);
            if (entity.getType().is(tag)) {
                return true;
            }
        }
        return false;
    }

    public String sourceTag() {
        return "organdefaults:loadout/" + id.getNamespace() + "/" + id.getPath();
    }

    public record OrganOption(ResourceLocation itemId, int weight, Float minMaxHealth) {
    }

    public record PointAdjustment(String sourceTag, String pointKey, long amount) {
        public PointAdjustment {
            sourceTag = sourceTag == null || sourceTag.isBlank() ? null : sourceTag;
        }
    }
}
