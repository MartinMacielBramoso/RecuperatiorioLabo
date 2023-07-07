package ar.edu.utn.frc.tup.lciii.services.impl;

import ar.edu.utn.frc.tup.lciii.models.Card;
import ar.edu.utn.frc.tup.lciii.models.CardSuit;
import ar.edu.utn.frc.tup.lciii.models.Deck;
import ar.edu.utn.frc.tup.lciii.services.DeckService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Service
public class DeckServiceImpl implements DeckService {

    @Override
    public Deck createDeck() {
        Deck deck = new Deck(new ArrayList<>());
        for(CardSuit cardSuit :CardSuit.values()) {
            for(int i = 1; i < 3; i++) {
                deck.getCards().add(new Card(cardSuit, i, BigDecimal.valueOf(i)));
            }
        }
        return deck;
    }

    @Override
    public void shuffleDeck(Deck deck) {
        Collections.shuffle(deck.getCards());
    }

    @Override
    public Card takeCard(Deck deck, Integer deckIndex) {
        if (deckIndex < 0 || deckIndex >= deck.getCards().size()) {
            throw new IllegalArgumentException("Índice de mazo inválido");
        }
        return deck.getCards().remove((int) deckIndex);
    }
}
