package cn.kuzuanpa.organdefaults.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class DefaultLoadoutLoader extends SimplePreparableReloadListener<List<DefaultLoadoutDefinition>> {
    public static final DefaultLoadoutLoader INSTANCE = new DefaultLoadoutLoader();
    private static final Gson GSON = new GsonBuilder().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DIRECTORY = "default_loadouts";

    private DefaultLoadoutLoader() {
    }

    @Override
    protected List<DefaultLoadoutDefinition> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        List<DefaultLoadoutDefinition> definitions = new ArrayList<>();
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(DIRECTORY, path -> path.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement element = GsonHelper.fromJson(GSON, reader, JsonElement.class);
                if (element == null || !element.isJsonObject()) {
                    continue;
                }
                definitions.add(readDefinition(entry.getKey(), element.getAsJsonObject()));
            } catch (Exception exception) {
                LOGGER.warn("Failed to load default loadout {}: {}", entry.getKey(), exception.getMessage());
            }
        }
        definitions.sort((left, right) -> Integer.compare(right.priority(), left.priority()));
        return definitions;
    }

    @Override
    protected void apply(List<DefaultLoadoutDefinition> definitions, ResourceManager resourceManager, ProfilerFiller profiler) {
        DefaultLoadoutRegistry.setLoadouts(definitions);
        LOGGER.info("Loaded {} organDefaults loadout(s): {}", definitions.size(),
                definitions.stream().map(definition -> definition.id().toString()).toList());
    }

    private static DefaultLoadoutDefinition readDefinition(ResourceLocation fileId, JsonObject obj) {
        int priority = GsonHelper.getAsInt(obj, "priority", 0);
        Set<ResourceLocation> entityTypes = readLocationSet(obj, "entity_types");
        Set<ResourceLocation> entityTags = readLocationSet(obj, "entity_tags");
        Map<ResourceLocation, Map<Integer, List<DefaultLoadoutDefinition.OrganOption>>> placements = readPlacements(obj, fileId);
        List<DefaultLoadoutDefinition.PointAdjustment> pointAdjustments = readPointAdjustments(obj, fileId);
        return new DefaultLoadoutDefinition(toDefinitionId(fileId), priority, entityTypes, entityTags, placements, pointAdjustments);
    }

    private static Map<ResourceLocation, Map<Integer, List<DefaultLoadoutDefinition.OrganOption>>> readPlacements(JsonObject obj, ResourceLocation fileId) {
        if (!obj.has("placements")) {
            return Map.of();
        }
        JsonObject placementsObj = GsonHelper.getAsJsonObject(obj, "placements");
        Map<ResourceLocation, Map<Integer, List<DefaultLoadoutDefinition.OrganOption>>> placements = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> bodyPartEntry : placementsObj.entrySet()) {
            if (!bodyPartEntry.getValue().isJsonObject()) {
                continue;
            }
            ResourceLocation bodyPartId = ResourceLocation.tryParse(bodyPartEntry.getKey());
            if (bodyPartId == null) {
                LOGGER.warn("Skipping placement group with invalid body part {} in {}", bodyPartEntry.getKey(), fileId);
                continue;
            }
            Map<Integer, List<DefaultLoadoutDefinition.OrganOption>> slotOptions =
                    readSlotOptions(bodyPartId, bodyPartEntry.getValue().getAsJsonObject(), fileId);
            if (!slotOptions.isEmpty()) {
                placements.put(bodyPartId, slotOptions);
            }
        }
        return placements;
    }

    private static Map<Integer, List<DefaultLoadoutDefinition.OrganOption>> readSlotOptions(
            ResourceLocation bodyPartId, JsonObject slotsObj, ResourceLocation fileId
    ) {
        Map<Integer, List<DefaultLoadoutDefinition.OrganOption>> slotOptions = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> slotEntry : slotsObj.entrySet()) {
            if (!slotEntry.getValue().isJsonArray()) {
                continue;
            }
            Integer slotIndex = parseSlotIndex(slotEntry.getKey(), fileId, bodyPartId);
            if (slotIndex == null) {
                continue;
            }
            List<DefaultLoadoutDefinition.OrganOption> options =
                    readOrganOptions(bodyPartId, slotIndex, slotEntry.getValue().getAsJsonArray(), fileId);
            if (!options.isEmpty()) {
                slotOptions.put(slotIndex, options);
            }
        }
        return slotOptions;
    }

    private static Integer parseSlotIndex(String key, ResourceLocation fileId, ResourceLocation bodyPartId) {
        try {
            int slotIndex = Integer.parseInt(key);
            if (slotIndex < 0) {
                LOGGER.warn("Skipping negative slot index {} for {} in {}", key, bodyPartId, fileId);
                return null;
            }
            return slotIndex;
        } catch (NumberFormatException exception) {
            LOGGER.warn("Skipping invalid slot index {} for {} in {}", key, bodyPartId, fileId);
            return null;
        }
    }

    private static List<DefaultLoadoutDefinition.OrganOption> readOrganOptions(
            ResourceLocation bodyPartId, int slotIndex, JsonArray array, ResourceLocation fileId
    ) {
        List<DefaultLoadoutDefinition.OrganOption> options = new ArrayList<>();
        int objectCount = 0;
        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                objectCount++;
            }
        }
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject optionObj = element.getAsJsonObject();
            ResourceLocation itemId = ResourceLocation.tryParse(GsonHelper.getAsString(optionObj, "id", null));
            if (itemId == null) {
                LOGGER.warn("Skipping organ option with invalid id for {}#{} in {}", bodyPartId, slotIndex, fileId);
                continue;
            }
            boolean hasWeight = optionObj.has("weight");
            int weight = hasWeight ? GsonHelper.getAsInt(optionObj, "weight", 1) : 1;
            if (weight <= 0) {
                LOGGER.warn("Skipping organ option {} with non-positive weight for {}#{} in {}", itemId, bodyPartId, slotIndex, fileId);
                continue;
            }
            if (objectCount > 1 && !hasWeight) {
                LOGGER.warn("Organ option {} for {}#{} in {} is missing weight in a multi-option slot; defaulting to 1",
                        itemId, bodyPartId, slotIndex, fileId);
            }
            Float minMaxHealth = optionObj.has("min_max_health")
                    ? Float.valueOf(GsonHelper.getAsFloat(optionObj, "min_max_health"))
                    : null;
            options.add(new DefaultLoadoutDefinition.OrganOption(itemId, weight, minMaxHealth));
        }
        return options;
    }

    private static List<DefaultLoadoutDefinition.PointAdjustment> readPointAdjustments(JsonObject obj, ResourceLocation fileId) {
        if (!obj.has("point_adjustments")) {
            return List.of();
        }
        JsonArray array = GsonHelper.getAsJsonArray(obj, "point_adjustments");
        List<DefaultLoadoutDefinition.PointAdjustment> adjustments = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject pointObj = element.getAsJsonObject();
            String source = GsonHelper.getAsString(pointObj, "source", null);
            String pointKey = readPointKey(pointObj, fileId);
            long amount = GsonHelper.getAsLong(pointObj, "amount", 0L);
            if (pointKey == null || amount == 0L) {
                continue;
            }
            adjustments.add(new DefaultLoadoutDefinition.PointAdjustment(source, pointKey, amount));
        }
        return adjustments;
    }

    private static String readPointKey(JsonObject pointObj, ResourceLocation fileId) {
        if (pointObj.has("point_key")) {
            return GsonHelper.getAsString(pointObj, "point_key");
        }
        String pointType = GsonHelper.getAsString(pointObj, "point_type", null);
        String pointId = GsonHelper.getAsString(pointObj, "point_id", null);
        if (pointType == null || pointId == null) {
            LOGGER.warn("Skipping point adjustment without point binding in {}", fileId);
            return null;
        }
        return pointType + ":" + pointId;
    }

    private static Set<ResourceLocation> readLocationSet(JsonObject obj, String fieldName) {
        if (!obj.has(fieldName)) {
            return Set.of();
        }
        JsonArray array = GsonHelper.getAsJsonArray(obj, fieldName);
        Set<ResourceLocation> locations = new HashSet<>();
        for (JsonElement element : array) {
            ResourceLocation id = ResourceLocation.tryParse(element.getAsString());
            if (id != null) {
                locations.add(id);
            }
        }
        return locations;
    }

    private static ResourceLocation toDefinitionId(ResourceLocation fileId) {
        String path = fileId.getPath();
        String prefix = DIRECTORY + "/";
        if (path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - 5);
        }
        return ResourceLocation.fromNamespaceAndPath(fileId.getNamespace(), path);
    }
}
