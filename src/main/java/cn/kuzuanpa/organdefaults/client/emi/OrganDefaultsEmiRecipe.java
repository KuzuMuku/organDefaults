package cn.kuzuanpa.organdefaults.client.emi;

import cn.kuzuanpa.organdefaults.common.DefaultLoadoutDefinition;
import cn.kuzuanpa.organdefaults.common.DefaultLoadoutDefinition.OrganOption;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

public final class OrganDefaultsEmiRecipe extends BasicEmiRecipe {
    private static final int WIDTH = 170;
    private static final int HEADER_HEIGHT = 24;
    private static final int ROW_HEIGHT = 22;
    private static final int GRID_START_X = 6;
    private static final int GRID_START_Y = HEADER_HEIGHT;
    private static final int GRID_COLUMNS = 8;

    private final Component entityLabel;
    private final EmiStack entityIngredient;
    private final List<SlotDisplay> slots;

    private OrganDefaultsEmiRecipe(
            ResourceLocation id,
            Component entityLabel,
            EmiStack entityIngredient,
            List<SlotDisplay> slots,
            List<EmiStack> outputs
    ) {
        super(OrganDefaultsEmiPlugin.DEFAULT_LOADOUT_CATEGORY, id, WIDTH,
                GRID_START_Y + Math.max(1, (slots.size() + GRID_COLUMNS - 1) / GRID_COLUMNS) * ROW_HEIGHT);
        this.entityLabel = entityLabel;
        this.entityIngredient = entityIngredient;
        this.slots = List.copyOf(slots);
        this.inputs = List.of(entityIngredient);
        this.outputs = List.copyOf(outputs);
    }

    public static List<OrganDefaultsEmiRecipe> expand(DefaultLoadoutDefinition loadout) {
        List<OrganDefaultsEmiRecipe> recipes = new ArrayList<>();
        int entityIndex = 0;
        for (ResourceLocation entityTypeId : loadout.entityTypes()) {
            recipes.add(createForEntity(loadout, entityTypeId, entityIndex++));
        }
        int tagIndex = 0;
        for (ResourceLocation tagId : loadout.entityTags()) {
            recipes.add(createForTag(loadout, tagId, tagIndex++));
        }
        return recipes;
    }

    private static OrganDefaultsEmiRecipe createForEntity(DefaultLoadoutDefinition loadout, ResourceLocation entityTypeId, int index) {
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityTypeId);
        Component entityLabel = entityType != null ? entityType.getDescription() : Component.literal(entityTypeId.toString());
        return new OrganDefaultsEmiRecipe(
                recipeId(loadout.id(), "entity", index, entityTypeId),
                entityLabel,
                EmiStack.of(getEntityIcon(entityType)),
                createSlots(loadout),
                collectOutputs(loadout));
    }

    private static OrganDefaultsEmiRecipe createForTag(DefaultLoadoutDefinition loadout, ResourceLocation tagId, int index) {
        return new OrganDefaultsEmiRecipe(
                recipeId(loadout.id(), "tag", index, tagId),
                Component.literal("#" + tagId),
                EmiStack.of(Items.NAME_TAG),
                createSlots(loadout),
                collectOutputs(loadout));
    }

    private static ResourceLocation recipeId(ResourceLocation loadoutId, String kind, int index, ResourceLocation targetId) {
        String targetPath = sanitizePath(targetId.getNamespace()) + "_" + sanitizePath(targetId.getPath());
        String path = "emi/default_loadouts/" + loadoutId.getPath() + "/" + kind + "/" + index
                + "_" + targetPath;
        return ResourceLocation.fromNamespaceAndPath(loadoutId.getNamespace(), path);
    }

    private static String sanitizePath(String value) {
        return value.toLowerCase(Locale.ROOT).replace(':', '_').replace('/', '_');
    }

    private static ItemStack getEntityIcon(EntityType<?> entityType) {
        if (entityType != null) {
            SpawnEggItem egg = SpawnEggItem.byId(entityType);
            if (egg != null) {
                return new ItemStack(egg);
            }
        }
        return new ItemStack(Items.NAME_TAG);
    }

    private static List<SlotDisplay> createSlots(DefaultLoadoutDefinition loadout) {
        List<SlotDisplay> slots = new ArrayList<>();
        int order = 1;
        for (Map.Entry<ResourceLocation, Map<Integer, List<OrganOption>>> bodyPartEntry : loadout.placements().entrySet()) {
            for (Map.Entry<Integer, List<OrganOption>> slotEntry : bodyPartEntry.getValue().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                EmiIngredient ingredient = createIngredient(slotEntry.getValue());
                if (!ingredient.isEmpty()) {
                    slots.add(new SlotDisplay(order++, slotEntry.getKey(), List.copyOf(slotEntry.getValue()), ingredient));
                }
            }
        }
        return slots;
    }

    private static EmiIngredient createIngredient(List<OrganOption> options) {
        List<EmiIngredient> candidates = options.stream()
                .map(OrganOption::itemId)
                .map(ForgeRegistries.ITEMS::getValue)
                .filter(Objects::nonNull)
                .map(EmiStack::of)
                .map(stack -> (EmiIngredient) stack)
                .toList();
        if (candidates.isEmpty()) {
            return EmiStack.EMPTY;
        }
        return candidates.size() == 1 ? candidates.get(0) : EmiIngredient.of(candidates);
    }

    private static List<EmiStack> collectOutputs(DefaultLoadoutDefinition loadout) {
        Set<Item> items = new LinkedHashSet<>();
        for (Map<Integer, List<OrganOption>> bodyPartPlacements : loadout.placements().values()) {
            for (List<OrganOption> options : bodyPartPlacements.values()) {
                for (OrganOption option : options) {
                    Item item = ForgeRegistries.ITEMS.getValue(option.itemId());
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
        }
        return items.stream().map(EmiStack::of).toList();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(entityIngredient, 4, 2)
                .drawBack(true)
                .catalyst(true)
                .appendTooltip(Component.literal("Loadout target"));
        widgets.addText(trim(entityLabel, 138), 26, 8, 0x404040, false);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 4);
        widgets.addText(Component.literal(Integer.toString(slots.size())), 80, 9, 0x707070, false);

        for (int i = 0; i < slots.size(); i++) {
            SlotDisplay slot = slots.get(i);
            int x = GRID_START_X + (i % GRID_COLUMNS) * 20;
            int y = GRID_START_Y + (i / GRID_COLUMNS) * ROW_HEIGHT;
            widgets.addSlot(slot.ingredient(), x, y)
                    .drawBack(true)
                    .recipeContext(this)
                    .appendTooltip(Component.literal("#" + slot.order()))
                    .appendTooltip(buildSlotSummary(slot));
        }
    }

    private static Component buildSlotSummary(SlotDisplay slot) {
        List<String> descriptions = new ArrayList<>();
        descriptions.add("slot " + slot.slotIndex());
        for (OrganOption option : slot.options()) {
            StringBuilder builder = new StringBuilder(option.itemId().toString())
                    .append(" x")
                    .append(option.weight());
            if (option.minMaxHealth() != null) {
                builder.append(" hp>=")
                        .append(String.format(Locale.ROOT, "%.1f", option.minMaxHealth()));
            }
            descriptions.add(builder.toString());
        }
        return Component.literal(String.join(" | ", descriptions));
    }

    private static FormattedCharSequence trim(Component text, int maxWidth) {
        Font font = Minecraft.getInstance().font;
        if (font.width(text) <= maxWidth) {
            return text.getVisualOrderText();
        }
        return Component.literal(font.substrByWidth(text, maxWidth - font.width("...")).getString() + "...").getVisualOrderText();
    }

    private record SlotDisplay(int order, int slotIndex, List<OrganOption> options, EmiIngredient ingredient) {
    }
}
