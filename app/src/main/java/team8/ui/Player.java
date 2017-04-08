package team8.ui;

import android.util.Log;

import java.io.*;
import java.util.*;

public class Player
{

  private int playerID;
  private int chipStack;
  private ArrayList<Card> Hand = new ArrayList<>();
  private int contribution = 0;
  private boolean bigBlind = false;
  private boolean smallBlind = false;
  private boolean dealer = false;
  private boolean hasFolded = false; // Is the player still playing
  private boolean TexasHoldEm;
  private boolean FiveCardDraw;

// Manually set the chipStack
  Player(int playerID, int chipStack)
  {
    this.playerID = playerID;
    this.chipStack = chipStack;
  }

// chipStack is automatically set to 100 if not set Manually
  Player(int playerID){
    this.playerID = playerID;
    this.chipStack = 100;
  }

  public int getChipStack()
  {
    return chipStack;
  }

// Takes multiple cards and adds them to the hand
  public void addToHand(ArrayList<Card> cards){
    for(int i = 0; i < cards.size(); i++){
      this.Hand.add(cards.get(i));
    }
  }

// Takes one card and adds it to the Hand
  public void addToHand(Card card)
  {
    this.Hand.add(card);
  }
// Returns the players current Hand
  public ArrayList<Card> getHand()
  {
    return this.Hand;
  }

// Returns true if the bet was possible, otherwise there was insufficient funds
  public boolean bet(int amount)
  {
    if(amount <= this.chipStack && !this.hasFolded)
    {
      this.chipStack -= amount;
      this.contribution += amount;
      return true;
    }
    else
      return false;
  }

// Returns true if a raise is possible, otherwise there was insufficient funds
  public boolean raise(int amount)
  {
    if(amount <= this.chipStack && !this.hasFolded)
    {
      this.chipStack -= amount;
      this.contribution += amount;
      return true;
    }
    else
      return false;
  }

// Similar to raise but its for call
  public boolean call(int amount)
  {
    if(amount <= this.chipStack && !this.hasFolded){
      this.chipStack -= amount;
      this.contribution += amount;
      return true;
    }
    else
      return false;
  }

// User chooses to fold
  public void fold()
  {
    this.hasFolded = true;
  }
// Makes this player the big Blind
  public void setBigBlind()
  {
    this.bigBlind = true;;
  }
// Make this player the small blind
  public void setSmallBlind(){
    this.smallBlind = true;
  }
// Make this player the dealer
  public void setDealer(){
    this.dealer = true;
  }
// End of game. Rest small blind and big blind
  public void resetBlind(){
    this.bigBlind = false;
    this.smallBlind = false;
  }
// Reset the dealer
  public void resetDealer(){
    this.dealer = false;
  }
// Returns how much a player has contributed to the pot
  public int getContribution(){
    return this.contribution;
  }
// Resets the contribution (usually means the end of a round)
  public void resetContribution(){
    this.contribution = 0;
  }
// Is the player still in the game
  public boolean hasFolded(){
    return this.hasFolded;
  }

  // Gets the players ID
  public int getPlayerID(){
    return this.playerID;
  }
}
