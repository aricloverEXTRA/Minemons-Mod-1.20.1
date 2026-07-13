package com.minemons.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Unified card asset manager handling textures, entities, and item art.
 * Consolidates and optimizes texture/entity/item lookup for cards.
 */
@Environment(EnvType.CLIENT)
public class CardAssetManager {

    private static final Map<String, Identifier> TEXTURES = new HashMap<>();
    private static final Map<String, String> ENTITY_PATHS = new HashMap<>();
    private static final Map<String, String> ITEM_PATHS = new HashMap<>();
    public static final Identifier FALLBACK = id("textures/entity/sheep/sheep.png");

    static {
        initializeAssets();
    }

    private static void initializeAssets() {
        // Neutral/Nexa
        registerEntity("sheep", "entity/sheep/sheep");
        registerEntity("shorn_sheep", "entity/sheep/sheep");
        registerEntity("cow", "entity/cow/cow");
        registerEntity("mooshroom", "entity/cow/cow");
        registerEntity("pig", "entity/pig/pig");
        registerEntity("chicken", "entity/chicken/temperate_chicken");
        registerEntity("fire_chicken", "entity/chicken/temperate_chicken");
        registerEntity("rabbit", "entity/sheep/sheep");
        registerEntity("llama", "entity/sheep/sheep");
        registerEntity("bee", "entity/bee/bee");
        registerEntity("ocelot", "entity/sheep/sheep");
        registerEntity("fox", "entity/fox/fox");
        registerEntity("panda", "entity/sheep/sheep");
        registerEntity("turtle", "entity/turtle/big_sea_turtle");
        registerEntity("dolphin", "entity/dolphin");
        registerEntity("axolotl", "entity/axolotl/axolotl_lucy");
        registerEntity("frog", "entity/frog/frog");
        registerEntity("tadpole", "entity/frog/frog");
        registerEntity("goat", "entity/goat/goat");
        registerEntity("camel", "entity/camel/camel");
        registerEntity("sniffer", "entity/sniffer/sniffer");
        registerEntity("sniffer_bloom", "entity/sniffer/sniffer");
        registerEntity("allay", "entity/allay/allay");
        registerEntity("allay_lumen", "entity/allay/allay");
        registerEntity("strider", "entity/strider/strider");
        registerEntity("parrot", "entity/parrot/parrot");
        registerEntity("bat", "entity/bat");
        registerEntity("glow_squid", "entity/squid/squid");
        registerEntity("squid", "entity/squid/squid");
        registerEntity("iron_golem", "entity/iron_golem/iron_golem");
        registerEntity("iron_colossus", "entity/iron_golem/iron_golem");
        registerEntity("snow_golem", "entity/snow_golem");
        registerEntity("wandering_trader", "entity/villager/villager");
        registerEntity("villager", "entity/villager/villager");

        // Embera
        registerEntity("blaze_imp", "entity/blaze");
        registerEntity("blaze_guard", "entity/blaze");
        registerEntity("magma_slime", "entity/creeper/creeper");
        registerEntity("ghastling", "entity/ghast/ghast");
        registerEntity("hoglin", "entity/hoglin/hoglin");
        registerEntity("zoglin", "entity/hoglin/hoglin");
        registerEntity("wither_cinder", "entity/skeleton/wither_skeleton");
        registerEntity("wither_remnant", "entity/skeleton/wither_skeleton");

        // Aqua
        registerEntity("water_sheep", "entity/sheep/sheep");
        registerEntity("river_cow", "entity/cow/cow");
        registerEntity("drowned", "entity/drowned/drowned");
        registerEntity("guardian", "entity/guardian/guardian");
        registerEntity("elder_guardian", "entity/guardian/elder_guardian");
        registerEntity("pufferfish", "entity/squid/squid");
        registerEntity("salmon", "entity/squid/squid");
        registerEntity("cod", "entity/squid/squid");
        registerEntity("tropical_fish", "entity/squid/squid");

        // Terra
        registerEntity("stone_cow", "entity/cow/cow");
        registerEntity("iron_sheep", "entity/sheep/sheep");
        registerEntity("rock_golem", "entity/iron_golem/iron_golem");
        registerEntity("ravager", "entity/ravager/ravager");
        registerEntity("silverfish", "entity/silverfish");
        registerEntity("endermite", "entity/endermite");
        registerEntity("zombie", "entity/zombie/zombie");
        registerEntity("husk", "entity/zombie/zombie");
        registerEntity("vindicator", "entity/illager/vindicator");
        registerEntity("pillager", "entity/illager/pillager");

        // Electra
        registerEntity("lightning_sheep", "entity/sheep/sheep");
        registerEntity("shock_spider", "entity/spider/spider");
        registerEntity("charged_cow", "entity/cow/cow");
        registerEntity("storm_vex", "entity/vex/vex");
        registerEntity("creeper", "entity/creeper/creeper");
        registerEntity("lodestone_sentinel", "entity/iron_golem/iron_golem");

        // Atmosa
        registerEntity("phantom_glider", "entity/phantom/phantom");
        registerEntity("vex_swarm", "entity/vex/vex");
        registerEntity("breeze", "entity/breeze/breeze");
        registerEntity("wither_skeleton", "entity/skeleton/wither_skeleton");
        registerEntity("skeleton", "entity/skeleton/skeleton");
        registerEntity("stray", "entity/skeleton/skeleton");
        registerEntity("wind_charge", "entity/breeze/breeze");

        // Flora
        registerEntity("vine_stalker", "entity/creeper/creeper");
        registerEntity("leaf_sprite", "entity/allay/allay");
        registerEntity("witch", "entity/witch");
        registerEntity("bogged", "entity/skeleton/skeleton");
        registerEntity("slime", "entity/creeper/creeper");
        registerEntity("warden_sprout", "entity/warden/warden");

        // Cosma
        registerEntity("void_sheep", "entity/sheep/sheep");
        registerEntity("endermite_scout", "entity/endermite");
        registerEntity("phantom_echo", "entity/phantom/phantom");
        registerEntity("shulker", "entity/shulker/shulker_purple");
        registerEntity("warden_echo", "entity/warden/warden");
        registerEntity("enderman", "entity/enderman/enderman");
        registerEntity("piglin_brute", "entity/piglin/piglin_brute");
        registerEntity("zombie_piglin", "entity/pig/zombified_piglin");

        // Luxa
        registerEntity("glow_sprite", "entity/allay/allay");
        registerEntity("beacon_construct", "entity/iron_golem/iron_golem");
        registerEntity("evoker", "entity/illager/evoker");

        // Crystra
        registerEntity("amethyst_golem", "entity/iron_golem/iron_golem");
        registerEntity("geode_crab", "entity/guardian/guardian");
        registerEntity("budding_shard", "entity/shulker/shulker_purple");
        registerEntity("crystal_spider", "entity/spider/spider");

        // Boss
        registerEntity("ender_dragon_hatchling", "entity/enderman/enderman");

        // Items (consumables)
        registerItem("mutton", "cooked_mutton");
        registerItem("beef", "cooked_beef");
        registerItem("bread", "bread");
        registerItem("golden_apple", "golden_apple");
        registerItem("enchanted_golden_apple", "enchanted_golden_apple");
        registerItem("cooked_porkchop", "cooked_porkchop");
        registerItem("cooked_chicken", "cooked_chicken");
        registerItem("melon_slice", "melon_slice");
        registerItem("honey_bottle", "honey_bottle");
        registerItem("mushroom_stew", "mushroom_stew");
        registerItem("golden_carrot", "golden_carrot");
        registerItem("chorus_fruit", "chorus_fruit");
        registerItem("totem_of_undying", "totem_of_undying");
        registerItem("potion_invis", "potion");
        registerItem("potion_strength", "potion");
        registerItem("potion_weakness", "potion");
        registerItem("potion_regen", "potion");

        // Trainers
        registerEntity("prof_sprout", "entity/villager/villager");
        registerEntity("librarian_lore", "entity/villager/villager");
        registerEntity("deck_rearrange", "entity/villager/villager");
        registerEntity("field_swap", "entity/villager/villager");
        registerEntity("element_surge", "entity/blaze");
        registerEntity("double_prize", "entity/illager/pillager");
        registerEntity("revival_herb", "entity/allay/allay");
        registerEntity("bench_heal", "entity/villager/villager");
        registerEntity("search_deck", "entity/villager/villager");
        registerEntity("disruptor", "entity/illager/pillager");
        registerEntity("quick_draw", "entity/villager/villager");
        registerEntity("trading_post", "entity/villager/villager");
        registerEntity("triple_prize", "entity/illager/pillager");
        registerEntity("evolution_crystal", "entity/shulker/shulker_purple");
        registerEntity("stamina_surge", "entity/allay/allay");
        registerEntity("switcheroo", "entity/enderman/enderman");
        registerEntity("mass_revive", "entity/villager/villager");

        // Places
        registerEntity("forest", "entity/bee/bee");
        registerEntity("meadow", "entity/bee/bee");
        registerEntity("volcano", "entity/blaze");
        registerEntity("flood", "entity/squid/squid");
        registerEntity("mountain", "entity/goat/goat");
        registerEntity("storm_field", "entity/breeze/breeze");
        registerEntity("void_zone", "entity/enderman/enderman");
        registerEntity("mushroom_fields", "entity/cow/cow");
        registerEntity("deep_dark", "entity/warden/warden");
        registerEntity("lush_caves", "entity/axolotl/axolotl_lucy");
        registerEntity("nether_wastes", "entity/strider/strider");
        registerEntity("soul_sand_valley", "entity/bat");
        registerEntity("crimson_forest", "entity/hoglin/hoglin");
        registerEntity("warped_forest", "entity/enderman/enderman");
        registerEntity("end_islands", "entity/enderman/enderman");
        registerEntity("stronghold", "entity/iron_golem/iron_golem");
        registerEntity("jungle", "entity/parrot/parrot");
        registerEntity("swamp", "entity/witch");
        registerEntity("desert", "entity/zombie/zombie");
        registerEntity("ice_spikes", "entity/snow_golem");
        registerEntity("snowy_taiga", "entity/snow_golem");
        registerEntity("badlands", "entity/goat/goat");
        registerEntity("warm_ocean", "entity/dolphin");
        registerEntity("cherry_grove", "entity/bee/bee");
    }

    private static void registerEntity(String cardId, String entityPath) {
        ENTITY_PATHS.put(cardId, entityPath);
        TEXTURES.put(cardId, id("textures/" + entityPath + ".png"));
    }

    private static void registerItem(String cardId, String itemId) {
        ITEM_PATHS.put(cardId, itemId);
        TEXTURES.put(cardId, id("textures/item/" + itemId + ".png"));
    }

    private static Identifier id(String path) {
        return new Identifier("minemons", path);
    }

    public static Identifier getTexture(String cardId) {
        return cardId != null ? TEXTURES.getOrDefault(cardId, FALLBACK) : FALLBACK;
    }

    public static String getItemId(String cardId) {
        return cardId != null ? ITEM_PATHS.get(cardId) : null;
    }

    public static boolean isEntityArt(String cardId) {
        return cardId != null && ENTITY_PATHS.containsKey(cardId);
    }

    public static EntityType<?> getEntityType(String cardId) {
        if (cardId == null) return null;
        String path = ENTITY_PATHS.get(cardId);
        if (path == null) return null;

        String local = path.startsWith("entity/") ? path.substring("entity/".length()) : path;
        if (local.endsWith(".png")) local = local.substring(0, local.length() - 4);

        String candidate = local.contains("/") ? local.substring(local.lastIndexOf('/') + 1) : local;
        Optional<EntityType<?>> type = Registries.ENTITY_TYPE.getOrEmpty(new Identifier("minecraft", candidate));
        if (type.isPresent()) return type.get();

        if (local.contains("/")) {
            String group = local.substring(0, local.indexOf('/'));
            return Registries.ENTITY_TYPE.getOrEmpty(new Identifier("minecraft", group)).orElse(null);
        }
        return null;
    }

    public static boolean isItemArt(String cardId) {
        if (cardId == null) return false;
        Identifier tex = TEXTURES.get(cardId);
        return tex != null && tex.getPath().startsWith("textures/item/");
    }
}
