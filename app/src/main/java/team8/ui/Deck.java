package team8.ui;

import java.io.*;
import java.util.*;

import team8.ui.Card;

// NOTE: The deck is not shuffled upon instantiation! You have to call the method

public class Deck
{

//Stores all the cards in the Deck
  private ArrayList<Card> deck = new ArrayList<Card>();
//Number of cards currently in the Deck
  private int cardsInDeck = 52;

// Class constructor that populates the deck
  Deck()
  {
    populateDeck();
  }

// Returns the top card
  public Card getCard()
  {
    // If there are no cards left to draw, return null
    if(cardsInDeck - 1 < 0)
    {
      return null;
    }
    cardsInDeck--;
    return deck.remove(0);
  }

// Returns the number of cards specified from the deck
  public ArrayList<Card> getCard(int numOfCards)
  {
    // If there are no cards left to draw, return null
    if(cardsInDeck - numOfCards < 0)
    {
      return null;
    }
    // Stores the cards to be returned
    ArrayList<Card> temp = new ArrayList<>();
    cardsInDeck -= numOfCards;
    for(int i = 0; i < numOfCards;i++){
      temp.add(deck.remove(0));
    }
    return temp;
  }
// Shuffles the deck
  public void shuffle()
  {
    int index;
    for(int i = 0; i < cardsInDeck;i++)
    {
      index = (int)(Math.random() * cardsInDeck);
      swap(i, index);
    }
  }

// Returns the number of cards left
  public int numCardsLeft()
  {
    return this.cardsInDeck;
  }
// Prints the contents of the deck (May not be useful)
  private void print()
  {
    for(int i = 0; i < cardsInDeck;i++){
      System.out.println(deck.get(i));
    }
  }
  // Populates the deck
  private void populateDeck()
  {
    deck.add(new Card(2, Suit.HEARTS));
    deck.add(new Card(3, Suit.HEARTS));
    deck.add(new Card(4, Suit.HEARTS));
    deck.add(new Card(5, Suit.HEARTS));
    deck.add(new Card(6, Suit.HEARTS));
    deck.add(new Card(7, Suit.HEARTS));
    deck.add(new Card(8, Suit.HEARTS));
    deck.add(new Card(9, Suit.HEARTS));
    deck.add(new Card(10, Suit.HEARTS));
    deck.add(new Card(11, Suit.HEARTS));
    deck.add(new Card(12, Suit.HEARTS));
    deck.add(new Card(13, Suit.HEARTS));
    deck.add(new Card(14, Suit.HEARTS));
    deck.add(new Card(2,Suit.CLUBS));
    deck.add(new Card(3,Suit.CLUBS));
    deck.add(new Card(4,Suit.CLUBS));
    deck.add(new Card(5,Suit.CLUBS));
    deck.add(new Card(6,Suit.CLUBS));
    deck.add(new Card(7,Suit.CLUBS));
    deck.add(new Card(8,Suit.CLUBS));
    deck.add(new Card(9,Suit.CLUBS));
    deck.add(new Card(10,Suit.CLUBS));
    deck.add(new Card(11,Suit.CLUBS));
    deck.add(new Card(12,Suit.CLUBS));
    deck.add(new Card(13,Suit.CLUBS));
    deck.add(new Card(14,Suit.CLUBS));
    deck.add(new Card(2,Suit.SPADES));
    deck.add(new Card(3,Suit.SPADES));
    deck.add(new Card(4,Suit.SPADES));
    deck.add(new Card(5,Suit.SPADES));
    deck.add(new Card(6,Suit.SPADES));
    deck.add(new Card(7,Suit.SPADES));
    deck.add(new Card(8,Suit.SPADES));
    deck.add(new Card(9,Suit.SPADES));
    deck.add(new Card(10,Suit.SPADES));
    deck.add(new Card(11,Suit.SPADES));
    deck.add(new Card(12,Suit.SPADES));
    deck.add(new Card(13,Suit.SPADES));
    deck.add(new Card(14,Suit.SPADES));
    deck.add(new Card(2,Suit.DIAMONDS));
    deck.add(new Card(3,Suit.DIAMONDS));
    deck.add(new Card(4,Suit.DIAMONDS));
    deck.add(new Card(5,Suit.DIAMONDS));
    deck.add(new Card(6,Suit.DIAMONDS));
    deck.add(new Card(7,Suit.DIAMONDS));
    deck.add(new Card(8,Suit.DIAMONDS));
    deck.add(new Card(9,Suit.DIAMONDS));
    deck.add(new Card(10,Suit.DIAMONDS));
    deck.add(new Card(11,Suit.DIAMONDS));
    deck.add(new Card(12,Suit.DIAMONDS));
    deck.add(new Card(13,Suit.DIAMONDS));
    deck.add(new Card(14,Suit.DIAMONDS));
  }
// Basic swap function (Only needed for shuffle)
  private void swap(int a, int b)
  {
      Card temp;
      temp = deck.get(a);
      deck.set(a, deck.get(b));
      deck.set(b, temp);
  }
}
