package com.minemons.registry;

import com.minemons.card.*;
import java.util.*;

/**
 * Central registry for all Minemons cards.
 * Assets reference raw GitHub URLs for vanilla MC textures where applicable.
 *
 * Texture base URL:
 * https://raw.githubusercontent.com/Faithful-Pack/Default-Java/1.21.11/assets/minecraft/textures/entity/
 */
public class CardRegistry {

    private static final Map<String, Card> ALL_CARDS = new LinkedHashMap<>();

    // -----------------------------------------------------------------------
    // MINEMON CARDS
    // -----------------------------------------------------------------------

    // === NEXA / NEUTRAL FARM MOBS ===
    private static void registerNexaMobs() {
        registerMinemon("sheep",       "Sheep",          Element.NEXA,   Rarity.COMMON,  50, 12,
            "Wool Padding", "Reduces incoming Embera damage by 20% (wool fire resistance).", true,
            "A fluffy Minemon that can use any energy. Its dense wool absorbs fire.",
            "sheep/sheep.png");

        registerMinemon("shorn_sheep", "Shorn Sheep",    Element.NEXA,   Rarity.COMMON,  40, 16,
            "Speed Shear", "Attack speed +1 extra action per match after evolving.", true,
            "After being sheared it's faster and leaner. Evolves from Sheep after 2 turns.",
            "sheep/sheep.png");

        registerMinemon("cow",         "Cow",            Element.NEXA,   Rarity.COMMON,  60, 10,
            "Beef Regen", "Heals 2 HP whenever benched.", true,
            "A sturdy Minemon. Its meaty constitution slowly restores HP while resting.",
            "cow/cow.png");

        registerMinemon("mooshroom",   "Mooshroom",      Element.FLORA,  Rarity.UNCOMMON, 55, 11,
            "Stew Confusion", "First attacker each match is confused for 1 turn.", false,
            "A mushroom-infused cow variant that baffles opponents with stew fumes.",
            "cow/mooshroom.png");

        registerMinemon("pig",         "Pig",            Element.NEXA,   Rarity.COMMON,  45, 13,
            "Oink Charge", "Can deal 1 extra attack at half damage once per match.", true,
            "Simple but scrappy. A reliable budget attacker for any deck.",
            "pig/pig.png");

        registerMinemon("chicken",     "Chicken",        Element.ATMOSA, Rarity.COMMON,  40, 11,
            "Flutter Dodge", "Dodges the very first attack each match.", false,
            "Quick and nimble, this feathery Minemon sidesteps incoming blows once.",
            "chicken/chicken.png");

        registerMinemon("rabbit",      "Rabbit",         Element.ATMOSA, Rarity.COMMON,  35, 10,
            "Hop Away", "Gains one free swap action per turn.", false,
            "Constantly hopping, it can reposition itself without costing an action.",
            "rabbit/white.png");

        registerMinemon("llama",       "Llama",          Element.TERRA,  Rarity.UNCOMMON, 65, 12,
            "Spit Disruption", "Can nullify an opponent's Trainer card once per match.", false,
            "Its signature spit disrupts even the most carefully planned support moves.",
            "llama/llama_brown.png");

        registerMinemon("bee",         "Bee",            Element.FLORA,  Rarity.UNCOMMON, 38, 14,
            "Counter-Sting", "When hit, poisons attacker for 2 turns. Dies after stinging.", false,
            "Fragile but fierce — attackers regret touching this buzzing guardian.",
            "bee/bee.png");

        registerMinemon("ocelot",      "Ocelot",         Element.NEXA,   Rarity.UNCOMMON, 42, 13,
            "Phantom Fear", "Phantom-line Minemons cannot target this card.", false,
            "Its natural predator aura terrifies ghost-type Minemons.",
            "ocelot/ocelot.png");

        registerMinemon("fox",         "Fox",            Element.FLORA,  Rarity.UNCOMMON, 40, 14,
            "Sly Peek", "On summon, peek at opponent's top 2 deck cards.", false,
            "Cunning and quick, it sniffs out what the opponent has planned.",
            "fox/fox.png");

        registerMinemon("panda",       "Panda",          Element.FLORA,  Rarity.UNCOMMON, 70, 10,
            "Sneeze Stun", "10% chance to stun enemy for 1 turn per attack.", false,
            "Lazily powerful — its random sneezes are startlingly effective.",
            "panda/panda.png");

        registerMinemon("turtle",      "Turtle",         Element.AQUA,   Rarity.UNCOMMON, 90, 8,
            "Shell Shield", "Blocks one attack's damage completely once per match.", false,
            "Slow but near-impenetrable. It outlasts flashy opponents with patience.",
            "turtle/big_sea_turtle.png");

        registerMinemon("dolphin",     "Dolphin",        Element.AQUA,   Rarity.UNCOMMON, 55, 14,
            "Aqua Boost", "Increases turn order speed of all allied Aqua cards.", false,
            "Its joyful leaps grant nearby water-types a burst of momentum.",
            "dolphin/dolphin.png");

        registerMinemon("axolotl",     "Axolotl",        Element.AQUA,   Rarity.RARE, 48, 12,
            "Play Dead", "Regenerates 20 HP when revived from fainted state.", false,
            "It plays dead when knocked out, then crawls back with renewed vigour.",
            "axolotl/axolotl.png");

        registerMinemon("frog",        "Frog",           Element.AQUA,   Rarity.COMMON, 44, 11,
            "Tongue Snatch", "Eats small-HP Minemons for +1 card draw on KO.", false,
            "A swampy snapper. If the opponent's card has low HP, it devours it whole.",
            "frog/frog_orange.png");

        registerMinemon("tadpole",     "Tadpole",        Element.AQUA,   Rarity.COMMON, 28, 8,
            "Evolving", "Becomes Frog after 2 turns on field.", false,
            "Tiny and defenceless — but it won't stay this way for long.",
            "frog/tadpole.png");

        registerMinemon("goat",        "Goat",           Element.TERRA,  Rarity.UNCOMMON, 58, 15,
            "Headbutt", "Can knock one opposing benched card back to opponent's hand.", false,
            "Stubborn and territorial, its headbutt disrupts the opponent's field.",
            "goat/goat.png");

        registerMinemon("camel",       "Camel",          Element.TERRA,  Rarity.UNCOMMON, 72, 11,
            "Desert Endurance", "Takes 10% less damage from EMBERA in arid Place cards.", false,
            "Built for the long game. It carries its team through harsh conditions.",
            "camel/camel.png");

        registerMinemon("sniffer",     "Sniffer",        Element.FLORA,  Rarity.RARE, 68, 9,
            "Ancient Dig", "On summon, pulls a random Place or Consumable from deck.", false,
            "Its ancient nose detects buried resources — and the right card at the right time.",
            "sniffer/sniffer.png");

        registerMinemon("allay",       "Allay",          Element.ATMOSA, Rarity.RARE, 36, 10,
            "Deliver", "Copies and duplicates one Consumable's effect once per match.", false,
            "It dances and delivers — doubling the power of your best potion.",
            "allay/allay.png");

        registerMinemon("strider",     "Strider",        Element.EMBERA, Rarity.UNCOMMON, 55, 15,
            "Lava Walk", "Takes no damage from Embera Place cards. Gains +3 ATK on lava fields.", false,
            "It strides across molten terrain that would melt anything else.",
            "strider/strider.png");

        registerMinemon("parrot",      "Parrot",         Element.ATMOSA, Rarity.RARE, 38, 12,
            "Mimic", "Copies the last ability used in this match (once per match).", false,
            "A feathered echo — whatever power was last used, it will use it again.",
            "parrot/parrot_red.png");

        registerMinemon("bat",         "Bat",            Element.COSMA,  Rarity.UNCOMMON, 32, 10,
            "Flee", "Retreats to hand instead of fainting when HP drops below 10 (once per match).", false,
            "It vanishes into the dark before the final blow lands.",
            "bat/bat.png");

        registerMinemon("glow_squid",  "Glow Squid",     Element.LUXA,   Rarity.UNCOMMON, 42, 11,
            "Illuminate", "Reveals one random card from opponent's hand.", false,
            "Its bioluminescent glow pierces through any opponent's veil of secrecy.",
            "squid/glow_squid.png");

        registerMinemon("squid",       "Squid",          Element.AQUA,   Rarity.COMMON, 40, 10,
            "Ink Cloud", "Lowers opponent's accuracy for 1 turn (-20% hit chance).", false,
            "It releases a burst of ink that blinds and disorients attackers.",
            "squid/squid.png");

        registerMinemon("iron_golem",  "Iron Golem",     Element.TERRA,  Rarity.EPIC, 120, 18,
            "Protector", "Guards the active Minemon — takes hits in its place once per turn.", false,
            "Legendary village guardian. Its iron fists and undying loyalty are unmatched.",
            "iron_golem/iron_golem.png");

        registerMinemon("snow_golem",  "Snow Golem",     Element.AQUA,   Rarity.UNCOMMON, 45, 9,
            "Snowball Knock", "Pushes attacker back instead of dealing normal damage.", false,
            "Its snowballs don't hurt — but the knockback ruins an opponent's combo.",
            "snow_golem/snow_golem.png");

        registerMinemon("wandering_trader", "Wandering Trader", Element.NEXA, Rarity.RARE, 38, 8,
            "Haggle", "Swap any card in your hand with a random card from your deck.", false,
            "He appears without warning and leaves just as fast — but always with a deal.",
            "villager/wandering_trader.png");
    }

    // === EMBERA (FIRE) ===
    private static void registerEmberaMobs() {
        registerMinemon("blaze_imp",   "Blaze Imp",      Element.EMBERA, Rarity.UNCOMMON, 50, 16,
            "Burn", "Deals 3 burn damage to attacker each turn for 2 turns.", false,
            "A small but furious firestarter. Its burn lingers long after the initial hit.",
            "blaze/blaze.png");

        registerMinemon("magma_slime",  "Magma Slime",   Element.EMBERA, Rarity.UNCOMMON, 60, 13,
            "Split", "When defeated, summons two 20-HP Slime tokens to the bench.", false,
            "It absorbs magma and multiplies. Defeating it just makes more problems.",
            "slime/magma_cube.png");

        registerMinemon("fire_chicken", "Fire Chicken",  Element.EMBERA, Rarity.COMMON, 44, 12,
            "Panic Flap", "On summon, randomly buffs one allied Minemon's attack by 4.", false,
            "Chaotically helpful. Its panicked flapping sends sparks in every direction.",
            "chicken/chicken.png");

        registerMinemon("ghastling",   "Ghastling",      Element.EMBERA, Rarity.RARE, 55, 17,
            "Fireball Recoil", "Fires a powerful ranged blast but deals 5 recoil to itself.", false,
            "A tiny ghast that packs a disproportionate punch at the cost of its own health.",
            "ghast/ghast.png");

        registerMinemon("hoglin",      "Hoglin",         Element.EMBERA, Rarity.UNCOMMON, 70, 16,
            "Aggressive Charge", "Ignores one defensive Place card's protection effect.", false,
            "It charges without mercy — terrain traps mean nothing to this beast.",
            "hoglin/hoglin.png");

        registerMinemon("zoglin",      "Zoglin",         Element.EMBERA, Rarity.UNCOMMON, 68, 17,
            "Undead Fury", "Immune to COSMA debuffs.", false,
            "A hoglin warped by the Nether's darkness. Fear itself cannot slow it.",
            "hoglin/zoglin.png");

        registerMinemon("wither_cinder", "Wither Cinder", Element.EMBERA, Rarity.EPIC, 85, 15,
            "Wither Decay", "Reduces one of opponent's stats by 1 each turn it's active.", false,
            "A living ember of the Wither. Its mere presence erodes the enemy's strength.",
            "wither/wither_armor.png");
    }

    // === AQUA (WATER) ===
    private static void registerAquaMobs() {
        registerMinemon("water_sheep",  "Water Sheep",   Element.AQUA,   Rarity.COMMON, 52, 11,
            "Flood Synergy", "Gets +3 ATK when Place: Flood is active.", true,
            "A sheep that wandered into a river one too many times. Now thrives in water.",
            "sheep/sheep.png");

        registerMinemon("river_cow",    "River Cow",     Element.AQUA,   Rarity.UNCOMMON, 65, 9,
            "Healing Current", "Heals entire bench by 1 HP each turn.", false,
            "Wades through rivers and heals its allies with the soothing current it stirs.",
            "cow/cow.png");

        registerMinemon("drowned",      "Drowned",       Element.AQUA,   Rarity.UNCOMMON, 55, 14,
            "Submerge", "Applies 'submerged' debuff to one opposing card, lowering its ATK by 3.", false,
            "It pulls enemies beneath the surface, sapping their fighting strength.",
            "drowned/drowned.png");

        registerMinemon("guardian",     "Guardian",      Element.AQUA,   Rarity.RARE, 75, 15,
            "Thorn Counter", "Deals 5 counter-damage to any Minemon that physically attacks it.", false,
            "Ancient aquatic guardian. Its laser eye and spiky body punish close-range attackers.",
            "guardian/guardian.png");

        registerMinemon("elder_guardian", "Elder Guardian", Element.AQUA, Rarity.EPIC, 110, 14,
            "Mind Fog", "Lowers all opponent bench Minemons' attack by 2 for 2 turns.", false,
            "Legendary deep-sea leviathan. Its psychic aura weakens every nearby enemy.",
            "guardian/elder_guardian.png");

        registerMinemon("pufferfish",   "Pufferfish",    Element.AQUA,   Rarity.COMMON, 35, 8,
            "Inflate", "When hit, poisons attacker for 3 turns.", false,
            "Looks harmless — touching it is the last mistake many Minemons make.",
            "fish/pufferfish.png");

        registerMinemon("salmon",       "Salmon",        Element.AQUA,   Rarity.COMMON, 32, 10,
            "Upstream Rush", "Deals +2 damage to Terra-type Minemons.", false,
            "Born to fight against the current. Earth-dwellers can't catch it.",
            "fish/salmon.png");

        registerMinemon("cod",           "Cod",           Element.AQUA,   Rarity.COMMON, 28, 8,
            "School", "Deals +1 damage per Aqua card on your bench.", false,
            "Alone it's tiny. With an aqua team behind it, its strikes add up fast.",
            "fish/cod.png");

        registerMinemon("tropical_fish", "Tropical Fish", Element.AQUA,   Rarity.COMMON, 25, 9,
            "Dazzle", "20% chance to confuse attacker for 1 turn (bright colours disorient).", false,
            "Its vivid colours are dazzling — and briefly disorienting to look at.",
            "fish/tropical_fish_a.png");
    }

    // === TERRA (EARTH) ===
    private static void registerTerraMobs() {
        registerMinemon("stone_cow",    "Stone Cow",     Element.TERRA,  Rarity.UNCOMMON, 75, 11,
            "Armoured Hide", "Blocks first attack each match completely.", false,
            "Its mineral-encrusted hide is nearly impenetrable on the first strike.",
            "cow/cow.png");

        registerMinemon("iron_sheep",   "Iron Sheep",    Element.TERRA,  Rarity.UNCOMMON, 70, 12,
            "Metal Wool", "Strong against CRYSTRA — reduces Crystra damage by 25%.", true,
            "Its metallic wool is harvested for armour. Crystals shatter against it.",
            "sheep/sheep.png");

        registerMinemon("rock_golem",   "Rock Golem",    Element.TERRA,  Rarity.RARE, 95, 14,
            "Taunt", "Forces opponents to target this card instead of others.", false,
            "A stone colossus that stands between its allies and all harm.",
            "iron_golem/iron_golem.png");

        registerMinemon("ravager",      "Ravager",       Element.TERRA,  Rarity.RARE, 88, 19,
            "Terrain Breaker", "Destroys the current active Place card on attack.", false,
            "It charges through everything — including the terrain itself.",
            "ravager/ravager.png");

        registerMinemon("silverfish",   "Silverfish",    Element.TERRA,  Rarity.COMMON, 22, 9,
            "Multiply", "Summons a second Silverfish token on the bench each turn it survives.", false,
            "Insignificant alone — but left unchecked, it fills the field quickly.",
            "silverfish/silverfish.png");

        registerMinemon("endermite",    "Endermite",     Element.TERRA,  Rarity.COMMON, 25, 10,
            "Tunnel Through", "Ignores Place terrain penalties.", false,
            "It burrows beneath terrain obstacles, nullifying environmental hazards.",
            "endermite/endermite.png");

        registerMinemon("zombie",       "Zombie",        Element.TERRA,  Rarity.COMMON, 50, 12,
            "Undead Persistence", "Survives one fatal hit with 1 HP (once per match).", false,
            "It keeps lurching forward even when it should be done.",
            "zombie/zombie.png");

        registerMinemon("husk",         "Husk",          Element.TERRA,  Rarity.COMMON, 55, 13,
            "Desert Desiccate", "Applies 'Dryness' debuff — disables AQUA passives for 1 turn.", false,
            "Dried out and relentless. It saps moisture from Aqua-type opponents.",
            "husk/husk.png");

        registerMinemon("vindicator",    "Vindicator",    Element.TERRA,  Rarity.RARE, 72, 20,
            "Johnny", "Gains +4 ATK if any Minemon on field has been KO'd this match.", false,
            "It comes with an axe and a grudge. Every fallen ally makes it angrier.",
            "illager/vindicator.png");

        registerMinemon("pillager",      "Pillager",      Element.TERRA,  Rarity.UNCOMMON, 55, 14,
            "Raid Captain", "On KO, draws 1 extra card for its side.", false,
            "Its banner marks territory. When it falls, it leaves your deck richer.",
            "illager/pillager.png");
    }

    // === ELECTRA (ELECTRICITY) ===
    private static void registerElectraMobs() {
        registerMinemon("lightning_sheep", "Lightning Sheep", Element.ELECTRA, Rarity.RARE, 52, 15,
            "Static Shock", "15% chance to skip opponent's next action on attack.", false,
            "A sheep that got struck by lightning and loved it. Shocks anything it touches.",
            "sheep/sheep.png");

        registerMinemon("shock_spider",  "Shock Spider",  Element.ELECTRA, Rarity.UNCOMMON, 46, 14,
            "Web Trap", "Lowers opponent active Minemon's attack by 4 for 2 turns.", false,
            "Its electric web clings and saps the fighting power of trapped prey.",
            "spider/spider.png");

        registerMinemon("charged_cow",   "Charged Cow",   Element.ELECTRA, Rarity.UNCOMMON, 58, 13,
            "Overcharge", "Boosts one allied Minemon's next attack by +6.", false,
            "Internally electrified. It channels stored energy into its allies.",
            "cow/cow.png");

        registerMinemon("storm_vex",     "Storm Vex",     Element.ELECTRA, Rarity.RARE, 45, 16,
            "Erratic", "Hard to target with single-target effects — 30% dodge chance.", false,
            "It zips and jolts unpredictably. Locking onto it is nearly impossible.",
            "vex/vex.png");

        registerMinemon("creeper",       "Creeper",       Element.ELECTRA, Rarity.UNCOMMON, 48, 18,
            "Self-Destruct", "Can deal 30 AOE damage to opponent's field but faints itself.", false,
            "The most feared Minemon on the field. Nobody wants to be near it when it goes.",
            "creeper/creeper.png");

        registerMinemon("lodestone_sentinel", "Lodestone Sentinel", Element.ELECTRA, Rarity.RARE, 70, 14,
            "Magnetic Pull", "Pulls one benched opponent card into active position.", false,
            "It magnetises the battlefield, dragging hidden threats into the open.",
            "iron_golem/iron_golem.png");
    }

    // === ATMOSA (AIR) ===
    private static void registerAtmosaMobs() {
        registerMinemon("phantom_glider", "Phantom Glider", Element.ATMOSA, Rarity.RARE, 50, 15,
            "Night Strike", "Deals +4 damage on 'night-themed' Place cards.", false,
            "A spectral flier that only reaches its full power in the dark.",
            "phantom/phantom.png");

        registerMinemon("vex_swarm",     "Vex Swarm",     Element.ATMOSA, Rarity.UNCOMMON, 42, 12,
            "Double Strike", "Attacks twice for half damage each hit.", false,
            "A chaotic swarm that strikes twice as fast — and twice as annoyingly.",
            "vex/vex_charging.png");

        registerMinemon("breeze",        "Breeze",        Element.ATMOSA, Rarity.RARE, 48, 14,
            "Wind Knock", "Knocks opponent back instead of dealing standard damage.", false,
            "A wind construct that deflects rather than destroys, buying precious time.",
            "breeze/breeze.png");

        registerMinemon("wither_skeleton", "Wither Skeleton", Element.ATMOSA, Rarity.RARE, 60, 16,
            "Wither Arrow", "Applies Wither status — 3 damage per turn for 3 turns.", false,
            "Its arrows carry the rot of the Nether. The wound keeps hurting long after.",
            "skeleton/wither_skeleton.png");

        registerMinemon("skeleton",      "Skeleton",      Element.ATMOSA, Rarity.COMMON, 42, 13,
            "Ranged Shot", "Can attack without being adjacent — ignores contact effects.", false,
            "A classic long-range attacker. It hits without putting itself at risk.",
            "skeleton/skeleton.png");

        registerMinemon("stray",         "Stray",         Element.ATMOSA, Rarity.UNCOMMON, 46, 13,
            "Slowness Arrow", "Reduces opponent speed — they lose one bonus action for 2 turns.", false,
            "A frozen skeleton from the tundra. Its arrows carry chilling slowness.",
            "skeleton/stray.png");

        registerMinemon("wind_charge",   "Wind Charge",   Element.ATMOSA, Rarity.UNCOMMON, 30, 15,
            "Explosive Gust", "Deals full damage then knocks opponent to bench (forced swap).", false,
            "A concentrated burst of air that hits hard and disrupts positioning.",
            "breeze/breeze.png");
    }

    // === FLORA (NATURE) ===
    private static void registerFloraMobs() {
        registerMinemon("vine_stalker",  "Vine Stalker",  Element.FLORA, Rarity.UNCOMMON, 54, 13,
            "Bind", "Prevents opponent from swapping out on their next turn.", false,
            "Its vines coil around the active Minemon, locking it in place.",
            "creeper/creeper.png");

        registerMinemon("leaf_sprite",   "Leaf Sprite",   Element.FLORA, Rarity.UNCOMMON, 40, 11,
            "Bloom Heal", "Heals 8 HP to self on summon.", false,
            "A forest spirit that arrives glowing with vitality and natural energy.",
            "allay/allay.png");

        registerMinemon("sniffer_bloom", "Sniffer Bloom", Element.FLORA, Rarity.RARE, 62, 11,
            "Grow", "Gains +2 ATK and +2 HP every turn it remains active.", false,
            "Patient and steady — the longer it stands, the more powerful it becomes.",
            "sniffer/sniffer.png");

        registerMinemon("warden_sprout", "Warden Sprout", Element.FLORA, Rarity.EPIC, 90, 13,
            "Sense", "Reveals all face-down Trainer tricks in opponent's hand.", false,
            "A tiny echo of the Warden — it senses everything that's hidden.",
            "warden/warden.png");

        registerMinemon("witch",         "Witch",         Element.FLORA, Rarity.UNCOMMON, 52, 12,
            "Potion Toss", "Randomly applies Heal, Debuff, or Slow to a target.", false,
            "Her potions are unpredictable but always consequential.",
            "witch/witch.png");

        registerMinemon("bogged",        "Bogged",        Element.FLORA, Rarity.UNCOMMON, 48, 13,
            "Poison Arrow", "Poisons attacker for 3 turns on any successful attack.", false,
            "A skeleton spawned from boggy swamps — its arrows carry a lingering blight.",
            "skeleton/skeleton.png");

        registerMinemon("slime",         "Slime",         Element.FLORA, Rarity.COMMON, 44, 11,
            "Sticky Body", "Slows any Minemon that attacks it (-1 action next turn).", false,
            "Bouncy and resilient. Hitting it just makes attackers sluggish.",
            "slime/slime.png");
    }

    // === COSMA (VOID) ===
    private static void registerCosmaMobs() {
        registerMinemon("void_sheep",    "Void Sheep",    Element.COSMA, Rarity.RARE, 50, 13,
            "Void Step", "Teleports out of danger once per match — avoids one attack.", false,
            "A sheep touched by the void. It phases through danger with eerie calm.",
            "sheep/sheep.png");

        registerMinemon("endermite_scout", "Endermite Scout", Element.COSMA, Rarity.UNCOMMON, 28, 10,
            "Hand Peek", "On summon, reveal 1 random card from opponent's hand.", false,
            "A void-touched creature that glimpses through the fabric of secrecy.",
            "endermite/endermite.png");

        registerMinemon("phantom_echo",  "Phantom Echo",  Element.COSMA, Rarity.RARE, 46, 13,
            "Echo Copy", "Copies the last fainted Minemon's passive for one turn.", false,
            "A ghost of what once was — it channels the fallen's final power.",
            "phantom/phantom.png");

        registerMinemon("shulker",       "Shulker",       Element.COSMA, Rarity.RARE, 80, 10,
            "Shell Lock", "Nearly immune until it attacks — defense drops to 0 when open.", false,
            "A paradox in armour — impossible to crack until it chooses to act.",
            "shulker/shulker_purple.png");

        registerMinemon("warden_echo",   "Warden Echo",   Element.COSMA, Rarity.MYTHIC, 150, 22,
            "Sonic Boom", "Telegraphs a massive hit — opponent gets 1 turn to prepare.", false,
            "The echo of the deep. Its power is so immense it gives a warning before striking.",
            "warden/warden.png");

        registerMinemon("enderman",      "Enderman",      Element.COSMA, Rarity.UNCOMMON, 60, 14,
            "Teleport", "Can swap itself with any benched Minemon freely once per turn.", false,
            "Never stays where you expect it. Field control incarnate.",
            "enderman/enderman.png");

        registerMinemon("piglin_brute",  "Piglin Brute",  Element.COSMA, Rarity.RARE, 80, 20,
            "Gold Rage", "Deals +6 damage if opponent played a Consumable this turn.", false,
            "A gold-obsessed brute from the Nether. Every Consumable your opponent uses fuels its anger.",
            "piglin/piglin_brute.png");

        registerMinemon("zombie_piglin",  "Zombie Piglin", Element.COSMA, Rarity.UNCOMMON, 62, 15,
            "Grudge", "If attacked first, retaliates with double damage once per match.", false,
            "Mostly peaceful — until you provoke it. Then it retaliates with everything it has.",
            "pig/zombified_piglin.png");
    }

    // === LUXA (LIGHT) ===
    private static void registerLuxaMobs() {
        registerMinemon("glow_sprite",   "Glow Sprite",   Element.LUXA, Rarity.UNCOMMON, 42, 10,
            "Light Heal", "Heals based on number of LUXA cards currently on field (+3 per card).", false,
            "It radiates warmth. The more light on the field, the stronger its healing.",
            "allay/allay.png");

        registerMinemon("beacon_construct", "Beacon Construct", Element.LUXA, Rarity.EPIC, 65, 12,
            "Buff All", "Gives all allied elements +1 elemental resistance for 1 turn.", false,
            "A manifestation of beacon light. Its aura fortifies every ally.",
            "iron_golem/iron_golem.png");

        registerMinemon("allay_lumen",   "Allay Lumen",   Element.LUXA, Rarity.RARE, 38, 11,
            "Redirect", "Sends one debuff back to whoever cast it.", false,
            "A luminous guardian that bounces bad energy right back at its source.",
            "allay/allay.png");

        registerMinemon("blaze_guard",   "Blaze Guard",   Element.LUXA, Rarity.RARE, 58, 14,
            "Holy Flame", "Deals +5 damage to COSMA-type Minemons.", false,
            "Blessed fire that burns especially bright against void-type opponents.",
            "blaze/blaze.png");

        registerMinemon("evoker",        "Evoker",        Element.LUXA, Rarity.EPIC, 65, 13,
            "Vex Summon", "Summons a Vex token to bench when played. Vex joins fight.", false,
            "A spellcasting illager that conjures spectral allies into battle.",
            "illager/evoker.png");

        registerMinemon("villager",      "Villager",      Element.LUXA, Rarity.COMMON, 38, 6,
            "Trade Up", "On summon, convert one card in hand to a random card of higher rarity.", false,
            "It drives a hard bargain. Even in battle, it turns average cards into great ones.",
            "villager/villager.png");
    }

    // === CRYSTRA (CRYSTAL) ===
    private static void registerCrystraMobs() {
        registerMinemon("amethyst_golem", "Amethyst Golem", Element.CRYSTRA, Rarity.EPIC, 85, 13,
            "Crystal Reflect", "Reflects 25% of damage taken back at attacker.", false,
            "A golem born of amethyst. Every blow fractures and rebounds.",
            "iron_golem/iron_golem.png");

        registerMinemon("geode_crab",    "Geode Crab",    Element.CRYSTRA, Rarity.RARE, 75, 11,
            "Harden", "Gains +2 defense each turn it remains active.", false,
            "Its shell gets thicker with time. The longer it waits, the harder it is to crack.",
            "guardian/guardian.png");

        registerMinemon("budding_shard", "Budding Shard", Element.CRYSTRA, Rarity.UNCOMMON, 48, 12,
            "Grow Token", "Spawns a small Crystra Shard token on the bench each turn.", false,
            "It slowly crystallises — producing tiny shards that clutter the field.",
            "shulker/shulker_purple.png");

        registerMinemon("crystal_spider", "Crystal Spider", Element.CRYSTRA, Rarity.UNCOMMON, 44, 13,
            "Prism Web", "Reduces opponent's attack by 3 for 2 turns on hit.", false,
            "Its crystalline web bends light and shatters opponent offensive combos.",
            "spider/spider.png");
    }

    // === BOSS / MYTHIC ===
    private static void registerBossMobs() {
        registerMinemon("ender_dragon_hatchling", "Dragon Hatchling", Element.COSMA, Rarity.MYTHIC, 130, 20,
            "Dragon Breath", "Deals 10 AOE to all opponent bench Minemons.", false,
            "A tiny but terrifying hatchling. The Cosma element bends around its presence.",
            "ender_dragon/ender_dragon.png");

        registerMinemon("wither_remnant", "Wither Remnant", Element.EMBERA, Rarity.MYTHIC, 120, 21,
            "Wither Stat Decay", "Decays one opponent stat per turn. Deals 21 base ATK.", false,
            "What remains of the Wither. It corrodes everything it touches.",
            "wither/wither_armor.png");

        registerMinemon("iron_colossus", "Iron Colossus", Element.TERRA, Rarity.EPIC, 140, 17,
            "Full Guard", "Protects entire bench from single-target effects for 1 turn.", false,
            "Towering and immovable. An entire bench shelters behind its shadow.",
            "iron_golem/iron_golem.png");
    }

    // -----------------------------------------------------------------------
    // CONSUMABLE CARDS
    // -----------------------------------------------------------------------

    private static void registerConsumables() {
        registerConsumable("mutton",        "Mutton",          Element.NEXA,   Rarity.COMMON,
            ConsumableCard.ConsumableEffect.HEAL, 15, 0,
            "Restore 15 HP to active Minemon. Also grants Stamina buff for 1 turn.",
            "items/cooked_mutton.png");

        registerConsumable("beef",          "Beef",            Element.NEXA,   Rarity.COMMON,
            ConsumableCard.ConsumableEffect.HP_BOOST, 20, 3,
            "Increase active Minemon's max HP by 20 for 3 turns.",
            "items/cooked_beef.png");

        registerConsumable("bread",         "Bread",           Element.NEXA,   Rarity.COMMON,
            ConsumableCard.ConsumableEffect.HEAL, 8, 0,
            "Restore 8 HP to active Minemon. Cheap and reliable.",
            "items/bread.png");

        registerConsumable("golden_apple",  "Golden Apple",    Element.LUXA,   Rarity.UNCOMMON,
            ConsumableCard.ConsumableEffect.SHIELD, 20, 2,
            "Grant active Minemon a shield that absorbs up to 20 damage for 2 turns.",
            "items/golden_apple.png");

        registerConsumable("enchanted_golden_apple", "Enchanted Golden Apple", Element.LUXA, Rarity.RARE,
            ConsumableCard.ConsumableEffect.SHIELD, 40, 3,
            "Grant a 40-damage shield for 3 turns. The legends are true.",
            "items/enchanted_golden_apple.png");

        registerConsumable("potion_invis",  "Invisibility Potion", Element.COSMA, Rarity.UNCOMMON,
            ConsumableCard.ConsumableEffect.FLEE, 1, 1,
            "Active card skips opponent's next targeting action.",
            "items/potion_bottle_empty.png");

        registerConsumable("potion_strength", "Strength Potion", Element.EMBERA, Rarity.UNCOMMON,
            ConsumableCard.ConsumableEffect.ATTACK_BOOST, 8, 2,
            "Boost active Minemon's attack by 8 for 2 turns.",
            "items/potion_bottle_empty.png");

        registerConsumable("potion_weakness", "Weakness Potion", Element.COSMA, Rarity.UNCOMMON,
            ConsumableCard.ConsumableEffect.DEBUFF_ENEMY, 6, 2,
            "Reduce opponent active Minemon's attack by 6 for 2 turns.",
            "items/potion_bottle_empty.png");

        registerConsumable("cooked_porkchop", "Cooked Porkchop", Element.NEXA, Rarity.COMMON,
            ConsumableCard.ConsumableEffect.STAMINA, 1, 1,
            "Grants an extra action this turn.",
            "items/cooked_porkchop.png");

        registerConsumable("cooked_chicken", "Cooked Chicken", Element.NEXA, Rarity.COMMON,
            ConsumableCard.ConsumableEffect.HEAL, 10, 0,
            "Restore 10 HP. A favourite of speedy Atmosa-type trainers.",
            "items/cooked_chicken.png");

        registerConsumable("melon_slice",  "Melon Slice",    Element.FLORA, Rarity.COMMON,
            ConsumableCard.ConsumableEffect.HEAL, 6, 0,
            "Heal 6 HP. Refreshing and light.",
            "items/melon_slice.png");

        registerConsumable("honey_bottle", "Honey Bottle",   Element.FLORA, Rarity.UNCOMMON,
            ConsumableCard.ConsumableEffect.HEAL, 12, 0,
            "Heals 12 HP and removes one active debuff from your Minemon.",
            "items/honey_bottle.png");

        registerConsumable("mushroom_stew", "Mushroom Stew", Element.FLORA, Rarity.UNCOMMON,
            ConsumableCard.ConsumableEffect.HP_BOOST, 10, 2,
            "Heals 10 HP and boosts max HP by 10 for 2 turns.",
            "items/mushroom_stew.png");

        registerConsumable("golden_carrot", "Golden Carrot",  Element.LUXA, Rarity.UNCOMMON,
            ConsumableCard.ConsumableEffect.ATTACK_BOOST, 5, 2,
            "Boosts attack by 5 for 2 turns. Sharper vision, sharper strikes.",
            "items/golden_carrot.png");

        registerConsumable("potion_regen", "Regeneration Potion", Element.FLORA, Rarity.RARE,
            ConsumableCard.ConsumableEffect.HEAL, 6, 3,
            "Heals 6 HP per turn for 3 turns.",
            "items/potion_bottle_empty.png");

        registerConsumable("chorus_fruit", "Chorus Fruit",   Element.COSMA, Rarity.UNCOMMON,
            ConsumableCard.ConsumableEffect.FLEE, 1, 1,
            "Teleports your active Minemon to the bench, avoiding an attack.",
            "items/chorus_fruit.png");

        registerConsumable("totem_of_undying", "Totem of Undying", Element.LUXA, Rarity.MYTHIC,
            ConsumableCard.ConsumableEffect.SHIELD, 999, 1,
            "Prevents your active Minemon from fainting once. Legendary.",
            "items/totem_of_undying.png");
    }

    // -----------------------------------------------------------------------
    // TRAINER CARDS
    // -----------------------------------------------------------------------

    private static void registerTrainers() {
        registerTrainer("prof_sprout",   "Professor Sprout's Research", Rarity.UNCOMMON,
            TrainerCard.TrainerEffect.DRAW_CARDS, 3,
            "Draw 3 cards from your deck.",
            "villager/villager.png");

        registerTrainer("librarian_lore", "Librarian's Lore", Rarity.UNCOMMON,
            TrainerCard.TrainerEffect.PEEK_DECK, 3,
            "Look at the top 3 cards of your deck without drawing.",
            "villager/librarian_villager.png");

        registerTrainer("deck_rearrange", "Deck Rearrange", Rarity.RARE,
            TrainerCard.TrainerEffect.REARRANGE_DECK, 5,
            "Rearrange the top 5 cards of your deck in any order.",
            "villager/villager.png");

        registerTrainer("field_swap",    "Field Swap",      Rarity.COMMON,
            TrainerCard.TrainerEffect.SWAP_FIELD, 1,
            "Move any benched Minemon to the active slot.",
            "villager/villager.png");

        registerTrainer("element_surge", "Element Surge",   Rarity.RARE,
            TrainerCard.TrainerEffect.BUFF_ELEMENT, 2,
            "Boost one element's damage output by 25% for 2 turns.",
            "villager/villager.png");

        registerTrainer("double_prize",  "Double Down",     Rarity.EPIC,
            TrainerCard.TrainerEffect.TAKE_EXTRA_PRIZE, 2,
            "Take 2 prize cards on your next Minemon KO.",
            "villager/villager.png");

        registerTrainer("revival_herb",  "Revival Herb",    Rarity.RARE,
            TrainerCard.TrainerEffect.REVIVE_MINEMON, 30,
            "Return a fainted Minemon to the bench with 30 HP.",
            "villager/villager.png");

        registerTrainer("bench_heal",    "Bench Medic",     Rarity.UNCOMMON,
            TrainerCard.TrainerEffect.HEAL_BENCH, 15,
            "Heal all benched Minemons by 15 HP.",
            "villager/cleric_villager.png");

        registerTrainer("search_deck",   "Card Search",     Rarity.RARE,
            TrainerCard.TrainerEffect.SEARCH_DECK, 1,
            "Search your deck for any 1 card and add it to your hand.",
            "villager/villager.png");

        registerTrainer("disruptor",     "Disruption Wave", Rarity.EPIC,
            TrainerCard.TrainerEffect.DISRUPT_OPPONENT, 1,
            "Force opponent to discard 2 cards from their hand.",
            "illager/pillager.png");

        registerTrainer("quick_draw",    "Quick Draw",      Rarity.COMMON,
            TrainerCard.TrainerEffect.DRAW_CARDS, 2,
            "Draw 2 cards immediately.",
            "villager/villager.png");

        registerTrainer("trading_post",  "Trading Post",    Rarity.UNCOMMON,
            TrainerCard.TrainerEffect.SEARCH_DECK, 2,
            "Search your deck for up to 2 Consumable cards.",
            "villager/wandering_trader.png");

        registerTrainer("triple_prize",  "Triple Prize",    Rarity.MYTHIC,
            TrainerCard.TrainerEffect.TAKE_EXTRA_PRIZE, 3,
            "Take 3 prize cards on your next KO. Mythic rarity.",
            "illager/pillager.png");

        registerTrainer("evolution_crystal", "Evolution Crystal", Rarity.RARE,
            TrainerCard.TrainerEffect.BUFF_ELEMENT, 1,
            "Permanently buff one Minemon's element by +1 tier for this match.",
            "shulker/shulker_purple.png");

        registerTrainer("stamina_surge", "Stamina Surge",   Rarity.UNCOMMON,
            TrainerCard.TrainerEffect.DRAW_CARDS, 1,
            "Draw 1 card and gain 1 extra action this turn.",
            "villager/villager.png");

        registerTrainer("switcheroo",    "Switcheroo",      Rarity.COMMON,
            TrainerCard.TrainerEffect.SWAP_FIELD, 1,
            "Swap your active and one benched Minemon without losing the action.",
            "villager/villager.png");

        registerTrainer("mass_revive",   "Mass Revive",     Rarity.MYTHIC,
            TrainerCard.TrainerEffect.REVIVE_MINEMON, 50,
            "Revive all fainted Minemons to bench with 50% HP.",
            "villager/cleric_villager.png");
    }

    // -----------------------------------------------------------------------
    // PLACE CARDS
    // -----------------------------------------------------------------------

    private static void registerPlaces() {
        registerPlace("forest",          "Forest",          Element.FLORA,    Rarity.COMMON,
            PlaceCard.PlaceEffect.BOOST_ELEMENT, Element.FLORA, 2,
            "Flora cards gain +2 ATK while this field is active.",
            "entity/chicken/chicken.png"); // placeholder

        registerPlace("meadow",          "Meadow",          Element.FLORA,    Rarity.COMMON,
            PlaceCard.PlaceEffect.BOOST_HEAL, null, 1,
            "Healing consumables restore +50% more HP.",
            "entity/chicken/chicken.png");

        registerPlace("volcano",         "Volcano",         Element.EMBERA,   Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.BOOST_ELEMENT, Element.EMBERA, 3,
            "Embera cards gain +3 ATK. Aqua passives are reduced.",
            "entity/blaze/blaze.png");

        registerPlace("flood",           "Flood",           Element.AQUA,     Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.BOOST_ELEMENT, Element.AQUA, 3,
            "Aqua cards gain +3 ATK. Fire-based attacks deal -2 damage.",
            "entity/squid/squid.png");

        registerPlace("mountain",        "Mountain",        Element.TERRA,    Rarity.COMMON,
            PlaceCard.PlaceEffect.BOOST_ELEMENT, Element.TERRA, 2,
            "Terra cards gain +2 ATK.",
            "entity/goat/goat.png");

        registerPlace("storm_field",     "Storm Field",     Element.ELECTRA,  Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.BOOST_ELEMENT, Element.ELECTRA, 2,
            "Electra cards gain +2 ATK. Atmosa effects are 50% less effective.",
            "entity/chicken/chicken.png");

        registerPlace("void_zone",       "Void Zone",       Element.COSMA,    Rarity.RARE,
            PlaceCard.PlaceEffect.SWAP_COST, null, 1,
            "Swapping cards costs an extra action. Cosma cards gain +2 ATK.",
            "entity/endermite/endermite.png");

        registerPlace("mushroom_fields", "Mushroom Fields", Element.FLORA,    Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.CHIP_DAMAGE_BOTH, null, 2,
            "Both active Minemons take 2 chip damage per turn (confusion spores).",
            "entity/cow/mooshroom.png");

        registerPlace("cherry_grove",    "Cherry Grove",    Element.LUXA,     Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.HEAL_ACTIVE_TURN, null, 1,
            "Active card heals 1 HP at the start of each turn.",
            "entity/chicken/chicken.png");

        registerPlace("deep_dark",       "Deep Dark",       Element.COSMA,    Rarity.RARE,
            PlaceCard.PlaceEffect.DISABLE_PASSIVES, null, 0,
            "Loud attacks risk summoning a hazard token. Passives are suppressed.",
            "entity/warden/warden.png");

        registerPlace("lush_caves",      "Lush Caves",      Element.FLORA,    Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.BOOST_HEAL, null, 1,
            "All Consumable card healing is boosted by +1 HP.",
            "entity/axolotl/axolotl.png");

        registerPlace("nether_wastes",   "Nether Wastes",   Element.EMBERA,   Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.BOOST_ELEMENT, Element.EMBERA, 0,
            "Embera cards are immune to Aqua elemental advantage here.",
            "entity/strider/strider.png");

        registerPlace("soul_sand_valley", "Soul Sand Valley", Element.COSMA,  Rarity.RARE,
            PlaceCard.PlaceEffect.SWAP_COST, null, 1,
            "Swapping any card costs 1 extra action.",
            "entity/bat/bat.png");

        registerPlace("crimson_forest",  "Crimson Forest",  Element.EMBERA,   Rarity.RARE,
            PlaceCard.PlaceEffect.CHIP_DAMAGE_BOTH, null, 3,
            "Both active Minemons take 3 chip damage per turn (hostile terrain).",
            "entity/hoglin/hoglin.png");

        registerPlace("warped_forest",   "Warped Forest",   Element.COSMA,    Rarity.EPIC,
            PlaceCard.PlaceEffect.DOUBLE_TRAINER, null, 0,
            "All Trainer card effects are doubled while this Place is active.",
            "entity/enderman/enderman.png");

        registerPlace("end_islands",     "End Islands",     Element.COSMA,    Rarity.EPIC,
            PlaceCard.PlaceEffect.EXTRA_PRIZE_ON_KO, null, 1,
            "Prize cards cost 1 more to claim but Cosma cards gain +2 ATK.",
            "entity/ender_dragon/ender_dragon.png");

        registerPlace("stronghold",      "Stronghold",      Element.TERRA,    Rarity.RARE,
            PlaceCard.PlaceEffect.REVEAL_TOP_CARD, null, 0,
            "Both players reveal their top deck card each turn.",
            "entity/iron_golem/iron_golem.png");

        registerPlace("jungle",          "Jungle",          Element.FLORA,    Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.EXTRA_DRAW, null, 1,
            "Both players draw 1 extra card on their first turn.",
            "entity/ocelot/ocelot.png");

        registerPlace("swamp",           "Swamp",           Element.FLORA,    Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.STATUS_EXTEND, null, 1,
            "Status effects last 1 extra turn.",
            "entity/witch/witch.png");

        registerPlace("desert",          "Desert",          Element.TERRA,    Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.WEAKEN_ELEMENT, Element.AQUA, 0,
            "Aqua-type passives are disabled while this Place is active.",
            "entity/husk/husk.png");

        registerPlace("ice_spikes",      "Ice Spikes",      Element.CRYSTRA,  Rarity.RARE,
            PlaceCard.PlaceEffect.BOOST_ELEMENT, Element.CRYSTRA, 0,
            "Crystra cards gain a 5-HP damage shield each turn.",
            "entity/snow_golem/snow_golem.png");

        registerPlace("snowy_taiga",     "Snowy Taiga",     Element.AQUA,     Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.WEAKEN_ELEMENT, Element.ELECTRA, 0,
            "Electra card effects are 50% less effective (frozen circuits).",
            "entity/snow_golem/snow_golem.png");

        registerPlace("badlands",        "Badlands",        Element.TERRA,    Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.BOOST_ELEMENT, Element.TERRA, 1,
            "Terra and Crystra cards gain +1 ATK.",
            "entity/goat/goat.png");

        registerPlace("warm_ocean",      "Warm Ocean",      Element.AQUA,     Rarity.UNCOMMON,
            PlaceCard.PlaceEffect.BOOST_HEAL, null, 0,
            "Healing consumables are 50% more effective.",
            "entity/dolphin/dolphin.png");
    }

    // -----------------------------------------------------------------------
    // HELPERS
    // -----------------------------------------------------------------------

    private static void registerMinemon(String id, String displayName, Element element, Rarity rarity,
                                         int maxHp, int baseAttack,
                                         String passive, String passiveDesc, boolean neutralEnergy,
                                         String desc, String texture) {
        ALL_CARDS.put(id, new MinemonCard(id, displayName, element, rarity,
                maxHp, baseAttack, passive, passiveDesc, neutralEnergy, desc, texture));
    }

    private static void registerConsumable(String id, String displayName, Element element, Rarity rarity,
                                            ConsumableCard.ConsumableEffect effect, int value, int duration,
                                            String desc, String texture) {
        ALL_CARDS.put(id, new ConsumableCard(id, displayName, element, rarity, effect, value, duration, desc, texture));
    }

    private static void registerTrainer(String id, String displayName, Rarity rarity,
                                         TrainerCard.TrainerEffect effect, int value,
                                         String desc, String texture) {
        ALL_CARDS.put(id, new TrainerCard(id, displayName, rarity, effect, value, desc, texture));
    }

    private static void registerPlace(String id, String displayName, Element element, Rarity rarity,
                                       PlaceCard.PlaceEffect effect, Element affected, int value,
                                       String desc, String texture) {
        ALL_CARDS.put(id, new PlaceCard(id, displayName, element, rarity, effect, affected, value, desc, texture));
    }

    // -----------------------------------------------------------------------
    // PUBLIC API
    // -----------------------------------------------------------------------

    public static void init() {
        registerNexaMobs();
        registerEmberaMobs();
        registerAquaMobs();
        registerTerraMobs();
        registerElectraMobs();
        registerAtmosaMobs();
        registerFloraMobs();
        registerCosmaMobs();
        registerLuxaMobs();
        registerCrystraMobs();
        registerBossMobs();
        registerConsumables();
        registerTrainers();
        registerPlaces();
    }

    public static Card getCard(String id) {
        return ALL_CARDS.get(id);
    }

    public static Collection<Card> getAllCards() {
        return Collections.unmodifiableCollection(ALL_CARDS.values());
    }

    public static List<Card> getCardsByType(CardType type) {
        List<Card> result = new ArrayList<>();
        for (Card c : ALL_CARDS.values()) if (c.getType() == type) result.add(c);
        return result;
    }

    public static List<Card> getCardsByElement(Element element) {
        List<Card> result = new ArrayList<>();
        for (Card c : ALL_CARDS.values()) if (c.getElement() == element) result.add(c);
        return result;
    }

    public static List<Card> getCardsByRarity(Rarity rarity) {
        List<Card> result = new ArrayList<>();
        for (Card c : ALL_CARDS.values()) if (c.getRarity() == rarity) result.add(c);
        return result;
    }

    /**
     * Register a custom card dynamically (for modder-added cards from JSON)
     */
    public static void registerCard(Card card) {
        if (card == null) throw new IllegalArgumentException("Card cannot be null");
        if (card.getId() == null || card.getId().isEmpty()) throw new IllegalArgumentException("Card ID cannot be null or empty");
        ALL_CARDS.put(card.getId(), card);
    }
}
