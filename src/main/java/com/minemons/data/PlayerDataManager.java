package com.minemons.data;

import com.minemons.MinemonsMain;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.*;

/**
 * Manages all per-player Minemons data using Fabric 1.20.1's PersistentState API.
 *
 * Data is stored in: <world>/data/minemons_player_data.dat
 *
 * In 1.20.1, PersistentState uses a static fromNbt factory method passed directly
 * to PersistentStateManager.getOrCreate() — there is no PersistentState.Type class.
 */
public class PlayerDataManager extends PersistentState {

    private static final String STORE_KEY = "minemons_player_data";
    public static final String NBT_KEY = "MinemonsData";

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    // ── PersistentState implementation ────────────────────────────────────────

    public PlayerDataManager() {}

    /** Deserialise from disk. Called by getOrCreate when the .dat file exists. */
    public static PlayerDataManager fromNbt(NbtCompound tag) {
        PlayerDataManager mgr = new PlayerDataManager();
        NbtCompound players = tag.getCompound("players");
        for (String uuidStr : players.getKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                mgr.playerDataMap.put(uuid, PlayerData.fromNbt(players.getCompound(uuidStr)));
            } catch (IllegalArgumentException ignored) {}
        }
        MinemonsMain.LOGGER.info("[Minemons] Loaded data for {} players.", mgr.playerDataMap.size());
        return mgr;
    }

    /** Serialise to disk. Called automatically on world save. */
    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        NbtCompound players = new NbtCompound();
        for (Map.Entry<UUID, PlayerData> e : playerDataMap.entrySet()) {
            players.put(e.getKey().toString(), e.getValue().toNbt());
        }
        tag.put("players", players);
        return tag;
    }

    // ── Singleton accessor ────────────────────────────────────────────────────

    private static PlayerDataManager instance = null;

    /**
     * Gets or creates the singleton from the overworld PersistentStateManager.
     * The 1.20.1 API: getOrCreate(fromNbt, constructor, key)
     */
    public static PlayerDataManager getInstance(MinecraftServer server) {
        if (instance == null) {
            PersistentStateManager psm = server
                    .getWorld(World.OVERWORLD)
                    .getPersistentStateManager();
            instance = psm.getOrCreate(
                    PlayerDataManager::fromNbt,
                    PlayerDataManager::new,
                    STORE_KEY
            );
        }
        return instance;
    }

    /** Release cached instance on server stop so the next start gets a clean load. */
    public static void clearInstance() {
        instance = null;
    }

    // ── Per-player API ────────────────────────────────────────────────────────

    /** Get (or create fresh) data for a player. Gives starter pack on first access. */
    public static PlayerData get(ServerPlayerEntity player) {
        PlayerDataManager mgr = getInstance(player.getServer());
        UUID uuid = player.getUuid();
        if (!mgr.playerDataMap.containsKey(uuid)) {
            PlayerData fresh = new PlayerData();
            giveStarterPack(fresh);
            mgr.playerDataMap.put(uuid, fresh);
            mgr.markDirty();
            MinemonsMain.LOGGER.info("[Minemons] Created fresh data for {}.", player.getName().getString());
        }
        return mgr.playerDataMap.get(uuid);
    }

    /** Ensure data is loaded for a player when they join. */
    public static void ensureLoaded(ServerPlayerEntity player) {
        get(player); // creates if absent, already calls markDirty for new players
    }

    /** Mark dirty so the next world save flushes data to disk. */
    public static void markDirty(MinecraftServer server) {
        getInstance(server).markDirty();
    }

    // ── Starter pack ──────────────────────────────────────────────────────────

    public static void giveStarterPack(PlayerData data) {
        // Minemons
        for (String id : new String[]{
            "sheep","sheep","sheep",
            "cow","cow","cow",
            "pig","pig","pig",
            "chicken","chicken","chicken",
            "rabbit","rabbit",
            "frog","frog",
            "tadpole","tadpole",
            "squid","squid",
            "zombie","zombie",
            "skeleton","skeleton",
            "silverfish","endermite"
        }) data.addCard(id);

        // Consumables
        for (String id : new String[]{
            "bread","bread","bread","bread",
            "mutton","mutton","mutton",
            "cooked_chicken","cooked_chicken",
            "melon_slice","melon_slice",
            "cooked_porkchop","cooked_porkchop"
        }) data.addCard(id);

        // Trainers
        for (String id : new String[]{
            "prof_sprout","prof_sprout",
            "quick_draw","quick_draw",
            "field_swap","field_swap",
            "bench_heal","librarian_lore"
        }) data.addCard(id);

        // Places
        for (String id : new String[]{
            "forest","forest","meadow","swamp","jungle"
        }) data.addCard(id);

        data.addXp(20);

        PlayerDeck starter = buildStarterDeck();
        if (starter != null) {
            data.addDeck(starter);
            data.setActiveDeckIndex(0);
        }
    }

    private static PlayerDeck buildStarterDeck() {
        PlayerDeck d = new PlayerDeck("Starter Deck");
        addN(d, "sheep",          4);
        addN(d, "cow",            4);
        addN(d, "pig",            4);
        addN(d, "chicken",        4);
        addN(d, "rabbit",         4);
        addN(d, "frog",           3);
        addN(d, "tadpole",        3);
        addN(d, "squid",          3);
        addN(d, "zombie",         3);
        addN(d, "skeleton",       3);
        addN(d, "silverfish",     2);
        addN(d, "endermite",      2);
        addN(d, "bread",          4);
        addN(d, "mutton",         3);
        addN(d, "cooked_chicken", 2);
        addN(d, "melon_slice",    2);
        addN(d, "cooked_porkchop",2);
        addN(d, "prof_sprout",    2);
        addN(d, "quick_draw",     2);
        addN(d, "field_swap",     2);
        addN(d, "bench_heal",     1);
        addN(d, "librarian_lore", 1);
        addN(d, "forest",         2);
        addN(d, "meadow",         1);
        addN(d, "swamp",          1);
        addN(d, "jungle",         1);
        return d.isValid() ? d : null;
    }

    private static void addN(PlayerDeck deck, String id, int n) {
        for (int i = 0; i < n; i++) deck.addCard(id);
    }
}
