# organDefaults

`organDefaults` provides default organ loadouts for entities that already have a body plan in `organAPI`.

It does not define body parts or base capacity.
Those belong in `data/<namespace>/organapi/body_plans/*.json`.

## What This Mod Does

When `organAPI` first resolves an entity's organ holder on the server:

1. `organDefaults` finds all matching loadouts and applies them from low priority to high priority.
2. It rolls the configured organ candidates and installs the selected organ items into the configured body-part slots.
3. It applies the configured point adjustments through `OepPointApi`.
4. It recomputes OEP effects.

This means developers can give mobs a default organ setup without requiring manual installation in gameplay.

## File Location

Put loadout files under:

```text
data/<namespace>/default_loadouts/<name>.json
```

Example:

```text
data/organdefaults/default_loadouts/minecraft_zombie.json
```

## JSON Format

```json
{
  "priority": 10,
  "entity_types": ["minecraft:zombie"],
  "entity_tags": [],
  "placements": {
    "organapi:head": {
      "0": [
        { "id": "organdefaults:baseline_organ" }
      ]
    },
    "organapi:chest": {
      "0": [
        { "id": "organdefaults:baseline_organ" }
      ]
    }
  },
  "point_adjustments": [
    {
      "source": "organdefaults:balance/minecraft_zombie",
      "point_type": "counter",
      "point_id": "organdefaults:baseline",
      "amount": 2
    },
    {
      "source": "organdefaults:balance/minecraft_zombie",
      "point_type": "attribute",
      "point_id": "minecraft:max_health",
      "amount": -4
    }
  ]
}
```

## Fields

| Field | Type | Required | Meaning |
| --- | --- | --- | --- |
| `priority` | integer | no | Higher priority loadouts are applied later, so they win if multiple loadouts target the same slot. Default is `0`. |
| `entity_types` | string array | no | Exact entity ids that this loadout matches, such as `minecraft:zombie`. |
| `entity_tags` | string array | no | Entity type tags that this loadout matches. |
| `placements` | object | no | Default organs to install, grouped by body part and slot index. |
| `point_adjustments` | object array | no | Manual point corrections or buffs applied after installation. |

If both `entity_types` and `entity_tags` are empty, the loadout matches nothing.

## Placement Format

`placements` is an object keyed by body part id.
Each body part contains an object keyed by slot index as a string.
Each slot index contains an array of candidate organs.

```json
{
  "organapi:head": {
    "0": [
      { "id": "organapi:sample_brain", "weight": 10 },
      { "id": "organapi:sample_brain", "weight": 4, "min_max_health": 40.0 }
    ]
  }
}
```

Candidate entries use this format:

```json
{ "id": "organapi:sample_brain", "weight": 10, "min_max_health": 40.0 }
```

| Field | Type | Required | Meaning |
| --- | --- | --- | --- |
| body part key | string | yes | Target body part id. This must exist in the entity's resolved body plan. |
| slot key | stringified integer | yes | Target slot index inside that body part. |
| `id` | string | yes | The item id to install if this candidate is selected. |
| `weight` | integer | conditionally | Selection weight. Optional only when the slot has exactly one candidate. |
| `min_max_health` | float | no | Minimum required entity max health. If the entity's max health is lower when generated, this candidate is ignored. |

Notes:

- `id` is an item id, not an organ definition id.
- The item must be accepted by the body part and should usually be in `#organapi:organs`.
- The target entity must already expose that body part through `organAPI` body plan data.
- When a slot has multiple candidates, missing `weight` causes a warning and defaults to `1`.
- If every candidate in a slot is filtered out, that slot stays empty.

## Point Adjustment Entries

Each `point_adjustments` entry uses this format:

```json
{
  "source": "organdefaults:balance/minecraft_zombie",
  "point_type": "attribute",
  "point_id": "minecraft:max_health",
  "amount": -4
}
```

| Field | Type | Required | Meaning |
| --- | --- | --- | --- |
| `source` | string | no | Explicit OEP source tag used for these points. |
| `point_type` | string | conditionally | Point type prefix, such as `attribute` or `counter`. |
| `point_id` | string | conditionally | Point id suffix, such as `minecraft:max_health`. |
| `point_key` | string | conditionally | Full point key, such as `attribute:minecraft:max_health`. |
| `amount` | long | yes | Signed point amount to apply. |

Rules:

- Use either `point_key`, or the `point_type` + `point_id` pair.
- Positive values buff the entity.
- Negative values offset the points added by default organs.
- If `source` is omitted, `organDefaults` uses a generated source based on the loadout id.

## Relationship With organAPI Body Plans

`organDefaults` should not be used to define body structure.

Use `organAPI` body plans for:

- which body parts an entity has
- each body part's base capacity
- accepted item tags for special body parts
- overview layout and sort order

Use `organDefaults` for:

- which organs an entity starts with
- manual balancing after those organs are installed

## Common Mistakes

| Problem | Cause |
| --- | --- |
| The mob gets no default organs | The loadout file is in the wrong datapack path or does not match the entity. |
| The slots exist but stay empty | The target organ item is not accepted by the body part, is not tagged correctly, or the body part id is wrong. |
| A placement fails silently in gameplay | Check the server log. `organDefaults` logs failed installations with the body part, slot, and failure message. |
| A rare boss-only organ never appears | Its `min_max_health` gate may be higher than the generated entity's max health. |
| The mob has wrong slot counts | The slot and capacity setup belongs in `organAPI` body plan data, not in `organDefaults`. |

## Example Files In This Project

- `data/organdefaults/default_loadouts/minecraft_zombie.json`
- `data/organdefaults/default_loadouts/minecraft_skeleton.json`
- `data/organdefaults/default_loadouts/minecraft_husk.json`
