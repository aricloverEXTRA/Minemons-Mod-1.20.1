package com.minemons.battle;

import com.minemons.card.*;
import com.minemons.data.PlayerData;
import com.minemons.data.PlayerDataManager;
import com.minemons.data.PlayerDeck;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

/**
 * Server-authoritative duel state machine.
 *
 * States: IDLE → MATCH_START → SHUFFLE_DECK → DRAW_PHASE →
 *         ACTION_PHASE → ATTACK_PHASE → PRIZE_CHECK → WIN_CHECK → END
 */
public class DuelState {

    public enum Phase {
        IDLE, MATCH_START, SHUFFLE_DECK, DRAW_PHASE,
        ACTION_PHASE, ATTACK_PHASE, PRIZE_CHECK, WIN_CHECK, END
    }

    // -----------------------------------------------------------------------
    // Per-player runtime state
    // -----------------------------------------------------------------------
    public static class PlayerSide {
        public final ServerPlayerEntity player;
        public final UUID playerId;

        public List<String> deck = new ArrayList<>();       // remaining draw pile (card IDs)
        public List<String> hand = new ArrayList<>();       // cards in hand
        public List<String> prizeCards = new ArrayList<>(); // prize cards not yet taken
        public List<ActiveMinemon> fieldCards = new ArrayList<>(); // up to 5 benched
        public ActiveMinemon activeCard = null;              // currently fighting
        public List<ActiveMinemon> faintedPile = new ArrayList<>();

        // If no Minemon is active, player becomes the card
        public int playerHp = 100;
        public boolean playerIsActive = false;

        public int turnsPlayed = 0;
        public boolean hasPlayedPlaceThisTurn = false;
        public int statusTurns = 0; // generic status counter
        public ActiveMinemon pendingStatusTarget = null;

        public PlayerSide(ServerPlayerEntity player) {
            this.player = player;
            this.playerId = player.getUuid();
        }

        public boolean isDefeated() {
            return prizeCards.isEmpty() == false && allPrizesGone()
                    || (activeCard == null && fieldCards.isEmpty() && playerIsActive && playerHp <= 0);
        }

        private boolean allPrizesGone() { return false; } // handled externally
    }

    // -----------------------------------------------------------------------
    // Active Minemon — runtime wrapper around a card with current HP
    // -----------------------------------------------------------------------
    public static class ActiveMinemon {
        public final String cardId;
        public int currentHp;
        public int attackBonus = 0;
        public int defenseBonus = 0;
        public int burnTurns = 0;
        public int poisonTurns = 0;
        public int shieldHp = 0;
        public int witherTurns = 0;
        public boolean isTaunting = false;
        public boolean isBound = false; // can't swap
        public boolean hasActedThisTurn = false;
        public int growStacks = 0; // for Sniffer Bloom, Geode Crab etc.

        public ActiveMinemon(String cardId, int maxHp) {
            this.cardId = cardId;
            this.currentHp = maxHp;
        }

        public boolean isFainted() { return currentHp <= 0; }

        public void takeDamage(int raw) {
            if (shieldHp > 0) {
                int absorbed = Math.min(shieldHp, raw);
                shieldHp -= absorbed;
                raw -= absorbed;
            }
            currentHp = Math.max(0, currentHp - raw);
        }
    }

    // -----------------------------------------------------------------------
    // Duel fields
    // -----------------------------------------------------------------------
    public final UUID duelId;
    public Phase phase = Phase.IDLE;
    public PlayerSide[] sides = new PlayerSide[2];
    public int currentTurn = 0; // 0 = sides[0]'s turn, 1 = sides[1]'s turn
    public PlaceCard activePlaceCard = null;
    public long shuffleSeed;

    public static final int PRIZE_CARDS = 6;
    public static final int STARTING_HAND = 7;
    public static final int MAX_FIELD = 5;

    public DuelState(UUID duelId, ServerPlayerEntity p1, ServerPlayerEntity p2, long seed) {
        this.duelId = duelId;
        this.sides[0] = new PlayerSide(p1);
        this.sides[1] = new PlayerSide(p2);
        this.shuffleSeed = seed;
    }

    // -----------------------------------------------------------------------
    // Phase transitions
    // -----------------------------------------------------------------------

    public void startMatch() {
        phase = Phase.MATCH_START;
        for (int i = 0; i < 2; i++) {
            PlayerSide side = sides[i];
            PlayerData data = PlayerDataManager.get(side.player);
            PlayerDeck deck = data.getActiveDeck();
            if (deck == null || !deck.isValid()) {
                // fallback — generate a basic starter
                side.deck = generateStarterDeck();
            } else {
                side.deck = deck.getShuffledCopy(shuffleSeed + i);
            }
        }
        phase = Phase.SHUFFLE_DECK;
        setupPrizeCards();
        dealStartingHands();
        phase = Phase.DRAW_PHASE;
        currentTurn = 0;
    }

    private void setupPrizeCards() {
        for (PlayerSide side : sides) {
            for (int i = 0; i < PRIZE_CARDS; i++) {
                if (!side.deck.isEmpty())
                    side.prizeCards.add(side.deck.remove(0));
            }
        }
    }

    private void dealStartingHands() {
        for (PlayerSide side : sides) {
            for (int i = 0; i < STARTING_HAND; i++) {
                drawCard(side);
            }
        }
        phase = Phase.ACTION_PHASE;
    }

    public boolean drawCard(PlayerSide side) {
        if (side.deck.isEmpty()) return false;
        side.hand.add(side.deck.remove(0));
        return true;
    }

    /** Returns which side wins: 0 or 1, or -1 if game is ongoing */
    public int checkWinCondition() {
        for (int i = 0; i < 2; i++) {
            if (sides[i].prizeCards.isEmpty()) return i; // took all prizes
        }
        // Check deck-out (no cards = lose)
        for (int i = 0; i < 2; i++) {
            if (sides[i].deck.isEmpty() && sides[i].hand.isEmpty()) return 1 - i;
        }
        // Check player HP
        for (int i = 0; i < 2; i++) {
            if (sides[i].playerIsActive && sides[i].playerHp <= 0) return 1 - i;
        }
        return -1;
    }

    /** Current active player side */
    public PlayerSide getCurrentSide()  { return sides[currentTurn]; }
    public PlayerSide getOpponentSide() { return sides[1 - currentTurn]; }

    /** End current player's turn */
    public void endTurn() {
        applyEndOfTurnEffects();
        currentTurn = 1 - currentTurn;
        sides[currentTurn].hasPlayedPlaceThisTurn = false;
        sides[currentTurn].turnsPlayed++;
        // Reset hasActed
        PlayerSide active = sides[currentTurn];
        if (active.activeCard != null) active.activeCard.hasActedThisTurn = false;
        for (ActiveMinemon m : active.fieldCards) m.hasActedThisTurn = false;

        phase = Phase.DRAW_PHASE;
        drawCard(getCurrentSide()); // draw 1 card per turn
        phase = Phase.ACTION_PHASE;
    }

    private void applyEndOfTurnEffects() {
        // Apply active Place card effects
        if (activePlaceCard != null && activePlaceCard.getEffect() == PlaceCard.PlaceEffect.CHIP_DAMAGE_BOTH) {
            int chip = activePlaceCard.getValue();
            for (PlayerSide side : sides) {
                if (side.activeCard != null) side.activeCard.takeDamage(chip);
            }
        }
        if (activePlaceCard != null && activePlaceCard.getEffect() == PlaceCard.PlaceEffect.HEAL_ACTIVE_TURN) {
            for (PlayerSide side : sides) {
                if (side.activeCard != null) {
                    MinemonCard card = getMinemonCard(side.activeCard.cardId);
                    if (card != null) {
                        side.activeCard.currentHp = Math.min(card.getMaxHp(),
                                side.activeCard.currentHp + activePlaceCard.getValue());
                    }
                }
            }
        }
        // Burn damage
        for (PlayerSide side : sides) {
            if (side.activeCard != null && side.activeCard.burnTurns > 0) {
                side.activeCard.takeDamage(3);
                side.activeCard.burnTurns--;
            }
            if (side.activeCard != null && side.activeCard.poisonTurns > 0) {
                side.activeCard.takeDamage(2);
                side.activeCard.poisonTurns--;
            }
            if (side.activeCard != null && side.activeCard.witherTurns > 0) {
                side.activeCard.attackBonus = Math.max(-10, side.activeCard.attackBonus - 1);
                side.activeCard.witherTurns--;
            }
        }
    }

    /** Execute an attack from current side's active card against opponent's active card */
    public AttackResult performAttack() {
        PlayerSide attacker = getCurrentSide();
        PlayerSide defender = getOpponentSide();

        phase = Phase.ATTACK_PHASE;

        if (attacker.activeCard == null && !attacker.playerIsActive) {
            // No active — player becomes the card
            attacker.playerIsActive = true;
        }

        int rawDamage;
        String attackerName;

        if (attacker.playerIsActive) {
            rawDamage = 10; // player default attack
            attackerName = attacker.player.getName().getString();
        } else {
            MinemonCard atkCard = getMinemonCard(attacker.activeCard.cardId);
            if (atkCard == null) return new AttackResult(false, 0, "Invalid card");
            rawDamage = atkCard.getBaseAttack() + attacker.activeCard.attackBonus;

            // Elemental advantage
            MinemonCard defCard = defender.activeCard != null ? getMinemonCard(defender.activeCard.cardId) : null;
            if (defCard != null) {
                float mult = atkCard.getElement().getMultiplierVs(defCard.getElement());
                rawDamage = Math.round(rawDamage * mult);
            }
            attackerName = atkCard.getDisplayName();
        }

        // Apply to defender
        if (defender.activeCard != null) {
            defender.activeCard.takeDamage(rawDamage);
        } else if (defender.playerIsActive) {
            defender.playerHp -= rawDamage;
        }

        attacker.activeCard.hasActedThisTurn = true;
        phase = Phase.PRIZE_CHECK;

        // Check faint
        boolean defenderFainted = (defender.activeCard != null && defender.activeCard.isFainted())
                || (defender.playerIsActive && defender.playerHp <= 0);

        if (defenderFainted && defender.activeCard != null) {
            String faintedId = defender.activeCard.cardId;
            defender.faintedPile.add(defender.activeCard);
            defender.activeCard = null;

            // Attacker takes a prize card
            String prizeCard = null;
            if (!attacker.prizeCards.isEmpty()) {
                prizeCard = attacker.prizeCards.remove(0);
            }
            // Auto-promote first bench card
            if (!defender.fieldCards.isEmpty()) {
                defender.activeCard = defender.fieldCards.remove(0);
            } else {
                defender.playerIsActive = true;
            }
            phase = Phase.WIN_CHECK;
            int winner = checkWinCondition();
            if (winner >= 0) {
                phase = Phase.END;
                return new AttackResult(true, rawDamage, faintedId + " fainted! " + sides[winner].player.getName().getString() + " wins!");
            }
            return new AttackResult(true, rawDamage, faintedId + " fainted! Prize taken: " + (prizeCard != null ? prizeCard : "none"));
        }

        phase = Phase.ACTION_PHASE;
        return new AttackResult(true, rawDamage, attackerName + " dealt " + rawDamage + " damage.");
    }

    public static class AttackResult {
        public final boolean success;
        public final int damage;
        public final String message;
        AttackResult(boolean success, int damage, String message) {
            this.success = success; this.damage = damage; this.message = message;
        }
    }

    public MinemonCard getMinemonCard(String id) {
        Card c = com.minemons.registry.CardRegistry.getCard(id);
        return (c instanceof MinemonCard) ? (MinemonCard) c : null;
    }

    /** Summon a card from hand to field or active slot */
    public boolean summonFromHand(PlayerSide side, int handIndex, boolean toActive) {
        if (handIndex < 0 || handIndex >= side.hand.size()) return false;
        String cardId = side.hand.get(handIndex);
        Card card = com.minemons.registry.CardRegistry.getCard(cardId);
        if (!(card instanceof MinemonCard mc)) return false;

        side.hand.remove(handIndex);
        ActiveMinemon am = new ActiveMinemon(cardId, mc.getMaxHp());

        if (toActive && side.activeCard == null) {
            side.activeCard = am;
        } else if (side.fieldCards.size() < MAX_FIELD) {
            side.fieldCards.add(am);
        } else {
            side.hand.add(handIndex, cardId); // return to hand
            return false;
        }
        return true;
    }

    /** Swap benched card to active slot */
    public boolean swapToActive(PlayerSide side, int benchIndex) {
        if (side.activeCard != null && side.activeCard.isBound) return false; // bound check
        if (benchIndex < 0 || benchIndex >= side.fieldCards.size()) return false;
        ActiveMinemon bench = side.fieldCards.remove(benchIndex);
        if (side.activeCard != null) side.fieldCards.add(0, side.activeCard);
        side.activeCard = bench;
        return true;
    }

    private List<String> generateStarterDeck() {
        List<String> deck = new ArrayList<>();
        String[] base = {"sheep", "cow", "pig", "chicken", "rabbit"};
        for (String id : base) {
            for (int i = 0; i < 4; i++) deck.add(id);
        }
        // Pad with trainers and consumables
        for (int i = 0; i < 8; i++) deck.add("quick_draw");
        for (int i = 0; i < 8; i++) deck.add("mutton");
        for (int i = 0; i < 4; i++) deck.add("bread");
        for (int i = 0; i < 4; i++) deck.add("meadow");
        for (int i = 0; i < 4; i++) deck.add("prof_sprout");
        for (int i = 0; i < 4; i++) deck.add("field_swap");
        return deck; // 60
    }
}
