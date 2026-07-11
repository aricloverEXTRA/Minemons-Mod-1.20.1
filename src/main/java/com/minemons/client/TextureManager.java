package com.minemons.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

/** Maps card IDs to bundled texture Identifiers. */
@Environment(EnvType.CLIENT)
public class TextureManager {

    private static final Map<String, Identifier> TEX = new HashMap<>();
    public static final Identifier FALLBACK = id("entity/sheep/sheep");

    static {
        // Neutral / Nexa
        e("sheep","entity/sheep/sheep"); e("shorn_sheep","entity/sheep/sheep");
        e("cow","entity/cow/cow"); e("mooshroom","entity/cow/cow");
        e("pig","entity/pig/pig"); e("chicken","entity/chicken/temperate_chicken");
        e("fire_chicken","entity/chicken/temperate_chicken");
        e("rabbit","entity/sheep/sheep"); e("llama","entity/sheep/sheep");
        e("bee","entity/bee/bee"); e("ocelot","entity/sheep/sheep");
        e("fox","entity/fox/fox"); e("panda","entity/sheep/sheep");
        e("turtle","entity/turtle/big_sea_turtle"); e("dolphin","entity/dolphin");
        e("axolotl","entity/axolotl/axolotl_lucy"); e("frog","entity/frog/frog");
        e("tadpole","entity/frog/frog"); e("goat","entity/goat/goat");
        e("camel","entity/camel/camel"); e("sniffer","entity/sniffer/sniffer");
        e("sniffer_bloom","entity/sniffer/sniffer"); e("allay","entity/allay/allay");
        e("allay_lumen","entity/allay/allay"); e("strider","entity/strider/strider");
        e("parrot","entity/parrot/parrot"); e("bat","entity/bat");
        e("glow_squid","entity/squid/squid"); e("squid","entity/squid/squid");
        e("iron_golem","entity/iron_golem/iron_golem"); e("iron_colossus","entity/iron_golem/iron_golem");
        e("snow_golem","entity/snow_golem"); e("wandering_trader","entity/villager/villager");
        e("villager","entity/villager/villager");
        // Embera
        e("blaze_imp","entity/blaze"); e("blaze_guard","entity/blaze");
        e("magma_slime","entity/creeper/creeper"); e("ghastling","entity/ghast/ghast");
        e("hoglin","entity/hoglin/hoglin"); e("zoglin","entity/hoglin/hoglin");
        e("wither_cinder","entity/skeleton/wither_skeleton");
        e("wither_remnant","entity/skeleton/wither_skeleton");
        // Aqua
        e("water_sheep","entity/sheep/sheep"); e("river_cow","entity/cow/cow");
        e("drowned","entity/drowned/drowned"); e("guardian","entity/guardian/guardian");
        e("elder_guardian","entity/guardian/elder_guardian");
        e("pufferfish","entity/squid/squid"); e("salmon","entity/squid/squid");
        e("cod","entity/squid/squid"); e("tropical_fish","entity/squid/squid");
        // Terra
        e("stone_cow","entity/cow/cow"); e("iron_sheep","entity/sheep/sheep");
        e("rock_golem","entity/iron_golem/iron_golem"); e("ravager","entity/ravager/ravager");
        e("silverfish","entity/silverfish"); e("endermite","entity/endermite");
        e("zombie","entity/zombie/zombie"); e("husk","entity/zombie/zombie");
        e("vindicator","entity/illager/vindicator"); e("pillager","entity/illager/pillager");
        // Electra
        e("lightning_sheep","entity/sheep/sheep"); e("shock_spider","entity/spider/spider");
        e("charged_cow","entity/cow/cow"); e("storm_vex","entity/vex/vex");
        e("creeper","entity/creeper/creeper"); e("lodestone_sentinel","entity/iron_golem/iron_golem");
        // Atmosa
        e("phantom_glider","entity/phantom/phantom"); e("vex_swarm","entity/vex/vex");
        e("breeze","entity/breeze/breeze"); e("wither_skeleton","entity/skeleton/wither_skeleton");
        e("skeleton","entity/skeleton/skeleton"); e("stray","entity/skeleton/skeleton");
        e("wind_charge","entity/breeze/breeze");
        // Flora
        e("vine_stalker","entity/creeper/creeper"); e("leaf_sprite","entity/allay/allay");
        e("witch","entity/witch"); e("bogged","entity/skeleton/skeleton");
        e("slime","entity/creeper/creeper"); e("warden_sprout","entity/warden/warden");
        // Cosma
        e("void_sheep","entity/sheep/sheep"); e("endermite_scout","entity/endermite");
        e("phantom_echo","entity/phantom/phantom"); e("shulker","entity/shulker/shulker_purple");
        e("warden_echo","entity/warden/warden"); e("enderman","entity/enderman/enderman");
        e("piglin_brute","entity/piglin/piglin_brute"); e("zombie_piglin","entity/pig/zombified_piglin");
        // Luxa
        e("glow_sprite","entity/allay/allay"); e("beacon_construct","entity/iron_golem/iron_golem");
        e("evoker","entity/illager/evoker");
        // Crystra
        e("amethyst_golem","entity/iron_golem/iron_golem"); e("geode_crab","entity/guardian/guardian");
        e("budding_shard","entity/shulker/shulker_purple"); e("crystal_spider","entity/spider/spider");
        // Boss
        e("ender_dragon_hatchling","entity/enderman/enderman");
        // Items (consumables)
        i("mutton","cooked_mutton"); i("beef","cooked_beef"); i("bread","bread");
        i("golden_apple","golden_apple"); i("enchanted_golden_apple","enchanted_golden_apple");
        i("cooked_porkchop","cooked_porkchop"); i("cooked_chicken","cooked_chicken");
        i("melon_slice","melon_slice"); i("honey_bottle","honey_bottle");
        i("mushroom_stew","mushroom_stew"); i("golden_carrot","golden_carrot");
        i("chorus_fruit","chorus_fruit"); i("totem_of_undying","totem_of_undying");
        i("potion_invis","potion"); i("potion_strength","potion");
        i("potion_weakness","potion"); i("potion_regen","potion");
        // Trainers
        e("prof_sprout","entity/villager/villager"); e("librarian_lore","entity/villager/villager");
        e("deck_rearrange","entity/villager/villager"); e("field_swap","entity/villager/villager");
        e("element_surge","entity/blaze"); e("double_prize","entity/illager/pillager");
        e("revival_herb","entity/allay/allay"); e("bench_heal","entity/villager/villager");
        e("search_deck","entity/villager/villager"); e("disruptor","entity/illager/pillager");
        e("quick_draw","entity/villager/villager"); e("trading_post","entity/villager/villager");
        e("triple_prize","entity/illager/pillager"); e("evolution_crystal","entity/shulker/shulker_purple");
        e("stamina_surge","entity/allay/allay"); e("switcheroo","entity/enderman/enderman");
        e("mass_revive","entity/villager/villager");
        // Places
        e("forest","entity/bee/bee"); e("meadow","entity/bee/bee");
        e("volcano","entity/blaze"); e("flood","entity/squid/squid");
        e("mountain","entity/goat/goat"); e("storm_field","entity/breeze/breeze");
        e("void_zone","entity/enderman/enderman"); e("mushroom_fields","entity/cow/cow");
        e("deep_dark","entity/warden/warden"); e("lush_caves","entity/axolotl/axolotl_lucy");
        e("nether_wastes","entity/strider/strider"); e("soul_sand_valley","entity/bat");
        e("crimson_forest","entity/hoglin/hoglin"); e("warped_forest","entity/enderman/enderman");
        e("end_islands","entity/enderman/enderman"); e("stronghold","entity/iron_golem/iron_golem");
        e("jungle","entity/parrot/parrot"); e("swamp","entity/witch");
        e("desert","entity/zombie/zombie"); e("ice_spikes","entity/snow_golem");
        e("snowy_taiga","entity/snow_golem"); e("badlands","entity/goat/goat");
        e("warm_ocean","entity/dolphin"); e("cherry_grove","entity/bee/bee");
    }

    private static void e(String id, String path) { TEX.put(id, id("textures/" + path + ".png")); }
    private static void i(String id, String item)  { TEX.put(id, id("textures/item/" + item + ".png")); }
    private static Identifier id(String p) { return new Identifier("minemons", p); }

    public static Identifier get(String cardId) {
        return cardId != null ? TEX.getOrDefault(cardId, FALLBACK) : FALLBACK;
    }
}
