package team8.ui;

import java.util.ArrayList;

public class Player
{

  private int playerID;
  protected int chipStack;
  public ArrayList<Card> allCards = new ArrayList<>();
  private ArrayList<Card> Hand = new ArrayList<>();
  private int contribution = 0;
  private boolean bigBlind = false;
  private boolean smallBlind = false;
  private boolean dealer = false;
  private boolean hasFolded = false; // Is the player still playing
  private boolean TexasHoldEm;
  private boolean FiveCardDraw;
  private boolean[] cardsDrawn;
    private boolean hasDrawn = false;

  // Manually set the chipStack
  Player(int playerID, int chipStack)
  {
    this.playerID = playerID;
    this.chipStack = chipStack;
      hasDrawn = false;
    cardsDrawn = new boolean[5];
    resetCardsDrawn();
  }


  public void addToAllCards(Card c)
  {
    allCards.add(c);
  }
  // chipStack is automatically set to 100 if not set Manually
  Player(int playerID)
  {
    this.playerID = playerID;
    this.chipStack = 100;
  }

  public int getChipStack()
  {
    return chipStack;
  }

  // Takes multiple cards and adds them to the hand
  public void addToHand(ArrayList<Card> cards)
  {
    for(int i = 0; i < cards.size(); i++)
    {
      this.Hand.add(cards.get(i));
    }
  }

  // Takes one card and adds it to the Hand
  public void addToHand(Card card)
  {
    this.Hand.add(card);
  }

  public void setHand(ArrayList<Card> newHand)
  {
      Hand = newHand;
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
      this.contribution = amount;
      return true;
    }
    else
      return false;
  }

  // Returns true if a raise is possible, otherwise there was insufficient funds
  public boolean raise(int amount)
  {
    if(amount - this.contribution <= this.chipStack && !this.hasFolded)
    {
      this.chipStack -= (amount - this.contribution);
      this.contribution = amount;
      return true;
    }
    else
      return false;
  }

  // Similar to raise but its for call
  public boolean call(int amount)
  {
    if(amount <= this.chipStack && !this.hasFolded)
    {
      this.chipStack -= amount;
      this.contribution = amount;
      return true;
    }
    else
      return false;
  }

    public void drawCard(int index)
    {
        cardsDrawn[index] = true;
    }

    public boolean cardDrawn(int index)
    {
        return cardsDrawn[index];
    }

    public void resetCardsDrawn()
    {
        for(int i = 0; i < 5; i++)
        {
            cardsDrawn[i] = false;
        }
    }

    public boolean hasDrawn()
    {
        return hasDrawn;
    }

    public void resetDrawn()
    {
        hasDrawn = false;
    }

  // User chooses to fold
  public void fold()
  {
    this.hasFolded = true;
  }
  // Makes this player the big Blind
  public void setBigBlind()
  {
    this.bigBlind = true;
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

  // when a new card is given to the ai when the Turn and River cards are dealt
  public Card [] bestHand (ArrayList<Card> all, int cardsPlayed)
  {
      int max = thisHand(all, 0, 1, 2, 3, 4);
      int [] maxVals = new int [5];

      for(int a = 0; a < 3; a++)
      {
          for(int b = 1; b < 4; b++)
          {
              for(int c = 2; c < 5; c++)
              {
                  for(int d = 3; d < 6; d++)
                  {
                      for(int e = 4; e < cardsPlayed; e++)
                      {
                          if(a < b && b < c && c < d && d < e)
                          {
                              int temp = thisHand(all, a, b, c, d, e);
                              if(temp > max) {
                                  max = temp;
                                  maxVals[0] = a;
                                  maxVals[1] = b;
                                  maxVals[2] = c;
                                  maxVals[3] = d;
                                  maxVals[4] = e;
                              }
                          }
                      }
                  }
              }
          }
      }
      Card [] toReturn = {all.get(maxVals[0]),all.get(maxVals[1]), all.get(maxVals[2]), all.get(maxVals[3]), all.get(maxVals[4])};
      return toReturn;
  }

    private int thisHand(ArrayList<Card> all, int a, int b, int c, int d, int e)
    {
        Card [] thisHand = {all.get(a), all.get(b), all.get(c), all.get(d), all.get(e)};
        return hand.score(thisHand);
    }
    public void addToChipstack(int amount)
    {
      chipStack += amount;
    }

    public void setDrawn()
    {
        hasDrawn = true;
    }
}
