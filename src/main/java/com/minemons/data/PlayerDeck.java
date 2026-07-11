package com.minemons.data;

import com.minemons.card.Card;
import com.minemons.registry.CardRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.*;

/**
 * Represents a player's deck — 60 cards, always shuffled deterministically.
 * Validates max-4 copies per card ID and exact 60-card count.
 */
public class PlayerDeck {

    public static final int DECK_SIZE = 60;
    public static final int MAX_COPIES = 4;

    private final List<String> cardIds = new ArrayList<>(); // ordered deck list (card IDs)
    private String deckName = "My Deck";

    public PlayerDeck() {}

    public PlayerDeck(String deckName) {
        this.deckName = deckName;
    }

    /** Add a card by ID, respecting the 4-copy limit. Returns true on success. */
    public boolean addCard(String cardId) {
        if (cardIds.size() >= DECK_SIZE) return false;
        long count = cardIds.stream().filter(cardId::equals).count();
        if (count >= MAX_COPIES) return false;
        cardIds.add(cardId);
        return true;
    }

    public boolean removeCard(String cardId) {
        return cardIds.remove(cardId);
    }

    public boolean isValid() {
        return cardIds.size() == DECK_SIZE;
    }

    public int getSize() { return cardIds.size(); }
    public String getDeckName() { return deckName; }
    /** Alias for getDeckName() — used by command code. */
    public String getName() { return deckName; }
    public void setDeckName(String name) { this.deckName = name; }
    public List<String> getCardIds() { return Collections.unmodifiableList(cardIds); }

    /**
     * Returns a shuffled copy of this deck using a seeded RNG.
     * The seed ensures determinism: same seed = same order.
     */
    public List<String> getShuffledCopy(long seed) {
        List<String> shuffled = new ArrayList<>(cardIds);
        Random rng = new Random(seed);
        Collections.shuffle(shuffled, rng);
        return shuffled;
    }

    public Map<String, Integer> getCardCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String id : cardIds) counts.merge(id, 1, Integer::sum);
        return counts;
    }

    /** Alias for getCardCounts() — used by command/packet code. */
    public Map<String, Integer> getCards() { return getCardCounts(); }

    // -----------------------------------------------------------------------
    // NBT serialisation
    // -----------------------------------------------------------------------

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putString("name", deckName);
        NbtList list = new NbtList();
        for (String id : cardIds) list.add(NbtString.of(id));
        tag.put("cards", list);
        return tag;
    }

    public static PlayerDeck fromNbt(NbtCompound tag) {
        PlayerDeck deck = new PlayerDeck(tag.getString("name"));
        NbtList list = tag.getList("cards", 8); // 8 = TAG_String
        for (int i = 0; i < list.size(); i++) {
            String id = list.getString(i);
            if (CardRegistry.getCard(id) != null) {
                deck.cardIds.add(id);
            }
        }
        return deck;
    }
}
