package com.minemons.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.*;

/**
 * Per-player persistent data: card collection, XP, active deck index.
 * Stored as NBT attached to the player entity via a persistent data component.
 */
public class PlayerData {

    private final List<PlayerDeck> decks = new ArrayList<>();
    private int activeDeckIndex = 0;
    private int xp = 0;

    // All cards owned by the player (as a multi-set)
    private final Map<String, Integer> ownedCards = new LinkedHashMap<>();

    public PlayerData() {}

    // -----------------------------------------------------------------------
    // XP
    // -----------------------------------------------------------------------

    public int getXp()                 { return xp; }
    public void addXp(int amount)      { xp = Math.max(0, xp + amount); }
    public boolean spendXp(int amount) {
        if (xp < amount) return false;
        xp -= amount;
        return true;
    }

    // -----------------------------------------------------------------------
    // Owned cards
    // -----------------------------------------------------------------------

    public void addCard(String cardId) {
        ownedCards.merge(cardId, 1, Integer::sum);
    }

    public void addCards(List<String> ids) {
        ids.forEach(this::addCard);
    }

    public boolean removeCard(String cardId) {
        Integer count = ownedCards.get(cardId);
        if (count == null || count <= 0) return false;
        if (count == 1) ownedCards.remove(cardId);
        else ownedCards.put(cardId, count - 1);
        return true;
    }

    public int getCardCount(String cardId) {
        return ownedCards.getOrDefault(cardId, 0);
    }

    public Map<String, Integer> getOwnedCards() {
        return Collections.unmodifiableMap(ownedCards);
    }

    // -----------------------------------------------------------------------
    // Decks
    // -----------------------------------------------------------------------

    public List<PlayerDeck> getDecks() { return decks; }

    public void addDeck(PlayerDeck deck) { decks.add(deck); }

    public PlayerDeck getActiveDeck() {
        if (decks.isEmpty()) return null;
        if (activeDeckIndex >= decks.size()) activeDeckIndex = 0;
        return decks.get(activeDeckIndex);
    }

    public void setActiveDeckIndex(int idx) {
        if (idx >= 0 && idx < decks.size()) activeDeckIndex = idx;
    }

    public int getActiveDeckIndex() { return activeDeckIndex; }

    // -----------------------------------------------------------------------
    // NBT
    // -----------------------------------------------------------------------

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putInt("xp", xp);
        tag.putInt("activeDeck", activeDeckIndex);

        NbtList deckList = new NbtList();
        for (PlayerDeck d : decks) deckList.add(d.toNbt());
        tag.put("decks", deckList);

        NbtCompound owned = new NbtCompound();
        for (Map.Entry<String, Integer> e : ownedCards.entrySet())
            owned.putInt(e.getKey(), e.getValue());
        tag.put("owned", owned);

        return tag;
    }

    public static PlayerData fromNbt(NbtCompound tag) {
        PlayerData data = new PlayerData();
        data.xp = tag.getInt("xp");
        data.activeDeckIndex = tag.getInt("activeDeck");

        NbtList deckList = tag.getList("decks", 10); // 10 = TAG_Compound
        for (int i = 0; i < deckList.size(); i++) {
            data.decks.add(PlayerDeck.fromNbt(deckList.getCompound(i)));
        }

        NbtCompound owned = tag.getCompound("owned");
        for (String key : owned.getKeys()) {
            data.ownedCards.put(key, owned.getInt(key));
        }
        return data;
    }
}
