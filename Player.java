import java.io.*;
import java.util.*;

public class Player{

  private int playerID;
  private int chipStack;
  private ArrayList<Card> Hand = new ArrayList<>();
  private int contribution = 0;
  private boolean bigBlind = false;
  private boolean smallBlind = false;
  private boolean dealer = false;
  private boolean inGame = false; // Has the player folded yet
  private boolean TexasHoldEm;
  private boolean FiveCardDraw;

// Manually set the chipStack
  Player(int playerID, int chipStack){
    this.playerID = playerID;
    this.chipStack = chipStack;
  }
// chipStack is automatically set to 100 if not set Manually
  Player(int playerID){
    this.playerID = playerID;
    this.chipStack = 100;
  }
// Takes multiple cards and adds them to the hand
  public void addToHand(ArrayList<Card> cards){
    for(int i = 0; i < cards.length(); i++){
      this.Hand.add(cards.get(i));
    }
  }
// Takes one card and adds it to the Hand
  public void addToHand(Card card){
    this.Hand.add(card);
  }
// Returns the players current Hand
  public ArrayList<Card> getHand(){
    return this.Hand;
  }
// Returns true if the raise was possible, otherwise there was insufficient funds
  public boolean raise(int amount){
    if(amount <= chipStack && this.inGame){
      this.chipStack -= amount;
      return true;
    }
    else
      return false;
  }
// Similar to raise but its for call
  public boolean call(int amount){
    if(amount <= chipStack && this.inGame){
      this.chipStack -= amount;
      return true;
    }
    else
      return false;
  }
// User chooses to fold
  public void fold(){
    this.inGame = false;
  }
// Makes this player the big Blind
  public void setBigBlind(){
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

}
