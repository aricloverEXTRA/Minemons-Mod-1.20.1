# Minemons

Minemons is a Minecraft 1.20.1 card-battle mod where vanilla mobs become collectible fighters. Build a 60-card deck, lead with one active Minemon, support it with up to five sideline Minemons, and win duels by claiming all six prize cards before your opponent does.

## Gameplay overview

- **Collect cards:** Minemon, Consumable, Trainer, and Place cards all fill different deck roles.
- **Build 60-card decks:** Mix reliable common cards, high-impact rare cards, healing food, disruption Trainers, and battlefield Places.
- **Duel other players:** One Minemon fights up front while up to five sit on the sideline. Knockouts claim prize cards; taking all six wins. If a player runs out of deck, they lose.
- **Use elements:** Terra, Flora, Electra, Embera, Aqua, Crystra, Luxa, Cosma, Atmosa, and Nexa give decks their identity.
- **Play around passives:** Mobs have unique passive hooks and abilities. Sheep can protect against fire-style pressure, food cards can heal or shield, and support mobs can draw, disrupt, or reposition.
- **Trade and tune:** Trade duplicates, chase missing rarity pieces, and keep improving your favorite element core.

## Visual rarity system

Cards are styled by rarity so powerful or special pulls feel visually distinct:

| Rarity | Visual direction | Gameplay direction |
| --- | --- | --- |
| Common | Clean grey frame | Efficient basics and deck glue |
| Uncommon | Element-tinted frame | Slightly stronger synergy pieces |
| Rare | Glow and shimmer | Strong but not auto-win cards |
| Epic | Bigger glow and premium frame | Build-around effects |
| Mythic | Shiny-style treatment | Showcase cards with carefully capped stats |

UI Lib is an **optional client-side visual dependency** planned for richer rarity effects. Minemons still works without it, but players who want the flashiest card UI can install it.

## Player requirements

### Fabric

Required:

- Minecraft **1.20.1**
- Java **17+**
- Fabric Loader **0.14.22+**
- Fabric API

Optional visual enhancement:

- UI Lib Fabric 1.20.1 - 0.3.6: <https://modrinth.com/mod/ui-lib/version/zTkzAdVf>

### Forge

Required:

- Minecraft **1.20.1**
- Java **17+**
- Forge **47.x**

Optional visual enhancement:

- UI Lib Forge 1.20.1 - 0.3.6: <https://modrinth.com/mod/ui-lib/version/GizE27iH>

> Forge support is still a scaffold while loader-specific registration, networking, and client hooks are migrated from the Fabric implementation.

## In-game tutorial controls

The client tutorial introduces the collection, card types, elements, deck building, dueling, passives, trading, and win conditions.

- **H** — revisit/restart the Minemons tutorial.
- **N** — advance to the next tutorial page.
- **B** — hide/skip the tutorial. You can press **H** later to bring it back.

## Data-driven cards

Built-in cards live under `common/src/main/resources/data/minemons/cards/` as one JSON file per card. The `cards.json` index lists the packaged card files to load.

Custom cards can be added with `config/minemons/custom_cards.json`. This keeps Minemons friendly to add-ons and pack authors because new cards do not require Java code changes.

## Repository modules

- `common`: shared assets and data loaded by both distributions.
- `fabric`: current Fabric implementation, entrypoints, networking, commands, UI, tutorial, and card rendering.
- `forge`: Forge loader scaffold and metadata for the ongoing loader migration.

## Building from source

Use the loader-specific Gradle tasks to build each distribution:

```bash
./gradlew :common:build
./gradlew :fabric:build
./gradlew :forge:build
```

Outputs are produced under each module's `build/libs/` directory.

## Credits

- MichemonsTeam
- Aric3435 — original coding assistance for Minemons
- SavageDaAverage — original coding assistance for Minemons
