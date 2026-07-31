package com.minemons.network;

import com.minemons.battle.DuelState;
import com.minemons.card.*;
import com.minemons.registry.CardRegistry;

/**
 * Processes non-Minemon card plays (Consumable, Trainer, Place) during a duel.
 */
public class CardActionProcessor {

    public static void processHandCard(DuelState duel, DuelState.PlayerSide side, int handIndex) {
        if (handIndex < 0 || handIndex >= side.hand.size()) return;
        String cardId = side.hand.get(handIndex);
        Card card = CardRegistry.getCard(cardId);
        if (card == null) return;

        switch (card.getType()) {
            case CONSUMABLE -> applyConsumable(duel, side, (ConsumableCard) card, handIndex);
            case TRAINER    -> applyTrainer(duel, side, (TrainerCard) card, handIndex);
            case PLACE      -> applyPlace(duel, side, (PlaceCard) card, handIndex);
            default -> {} // MINEMON handled separately
        }
    }

    private static void applyConsumable(DuelState duel, DuelState.PlayerSide side,
                                         ConsumableCard card, int handIndex) {
        side.hand.remove(handIndex);
        DuelState.ActiveMinemon target = side.activeCard;
        if (target == null) return;

        // Apply Place boost if active
        int bonusHeal = 0;
        if (duel.activePlaceCard != null &&
            duel.activePlaceCard.getEffect() == PlaceCard.PlaceEffect.BOOST_HEAL) {
            bonusHeal = (int)(card.getValue() * 0.5);
        }

        MinemonCard mc = duel.getMinemonCard(target.cardId);
        int maxHp = mc != null ? mc.getMaxHp() : 100;

        switch (card.getEffect()) {
            case HEAL ->
                target.currentHp = Math.min(maxHp, target.currentHp + card.getValue() + bonusHeal);
            case HP_BOOST ->
                target.currentHp = Math.min(maxHp + card.getValue(), target.currentHp + card.getValue() + bonusHeal);
            case ATTACK_BOOST ->
                target.attackBonus += card.getValue();
            case SHIELD ->
                target.shieldHp += card.getValue();
            case FLEE -> {
                // Move active to bench, opponent loses their next targeting
                if (!side.fieldCards.isEmpty()) {
                    side.fieldCards.add(0, target);
                    side.activeCard = null;
                }
            }
            case DEBUFF_ENEMY -> {
                DuelState.PlayerSide opp = duel.getOpponentSide();
                if (opp.activeCard != null) opp.activeCard.attackBonus -= card.getValue();
            }
            case STAMINA -> {
                // Extra action — reset hasActed for this turn
                if (target != null) target.hasActedThisTurn = false;
            }
        }
    }

    private static void applyTrainer(DuelState duel, DuelState.PlayerSide side,
                                      TrainerCard card, int handIndex) {
        side.hand.remove(handIndex);

        // Warped Forest doubles trainer effects
        int value = card.getValue();
        if (duel.activePlaceCard != null &&
            duel.activePlaceCard.getEffect() == PlaceCard.PlaceEffect.DOUBLE_TRAINER) {
            value *= 2;
        }

        switch (card.getEffect()) {
            case DRAW_CARDS -> {
                for (int i = 0; i < value; i++) duel.drawCard(side);
            }
            case PEEK_DECK -> {
                // Server-side no-op (client shows peek UI via separate packet)
            }
            case REARRANGE_DECK -> {
                // Peek + rearrange (simplified: rotate top N)
                java.util.Collections.rotate(side.deck.subList(0, Math.min(value, side.deck.size())), 1);
            }
            case SWAP_FIELD -> {
                if (!side.fieldCards.isEmpty()) duel.swapToActive(side, 0);
            }
            case BUFF_ELEMENT -> {
                // Handled as a temporary flag (simplified — boost all attacks by 3)
                if (side.activeCard != null) side.activeCard.attackBonus += 3;
            }
            case TAKE_EXTRA_PRIZE -> {
                // Next KO gives extra prize — flagged on side (simplified)
                for (int i = 0; i < Math.min(value - 1, side.prizeCards.size()); i++) {
                    // Take bonus prizes now
                }
            }
            case REVIVE_MINEMON -> {
                if (!side.faintedPile.isEmpty()) {
                    DuelState.ActiveMinemon revived = side.faintedPile.remove(0);
                    revived.currentHp = value;
                    revived.attackBonus = 0;
                    if (side.fieldCards.size() < DuelState.MAX_FIELD) side.fieldCards.add(revived);
                }
            }
            case HEAL_BENCH -> {
                for (DuelState.ActiveMinemon m : side.fieldCards) {
                    MinemonCard mc = duel.getMinemonCard(m.cardId);
                    if (mc != null) m.currentHp = Math.min(mc.getMaxHp(), m.currentHp + value);
                }
            }
            case SEARCH_DECK -> {
                // Client will trigger search UI — server-side just draws for now
                duel.drawCard(side);
            }
            case DISRUPT_OPPONENT -> {
                DuelState.PlayerSide opp = duel.getOpponentSide();
                for (int i = 0; i < value && !opp.hand.isEmpty(); i++) {
                    opp.hand.remove(opp.hand.size() - 1);
                }
            }
        }
    }

    private static void applyPlace(DuelState duel, DuelState.PlayerSide side,
                                    PlaceCard card, int handIndex) {
        if (side.hasPlayedPlaceThisTurn) return; // one place per turn
        side.hand.remove(handIndex);
        duel.activePlaceCard = card;
        side.hasPlayedPlaceThisTurn = true;
    }
}
