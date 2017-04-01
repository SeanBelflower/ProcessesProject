import java.util.*;
import Card;
import hand;
import Suit;

public class AI_Player extends Player
{
	public String name;
	public short chips;
	public short bet;
	private Random rand;
	private float confidence;
	private Card myHand[5]; 
	private char handValueIndex = 0;
	private Game game;
	private Card a;
	private Card b;
	private hand calculator;

	private final float betConfidenceThreshold = 0.7;

	public AI_Player(String name, int chips, Game game)
	{
		this.name = name;
		this.chips = chips;
		boldness = rand.nextInt(9)+1;
		confidence = 0.05*boldness;
		this.game = game;
	}

	// calculate confidence based on what cards have been played so far
	private void observeHand()
	{
		if(a == null || b == null)
		{
			a = game.draw();
			b = game.draw();
			myHand[0] = a;
			myHand[1] = b;
		}

		// pre-flop
		if(game.cardsPlayed == 0)
		{
			float startVal = startingHandValue();
			if(startVal > 2)
			{
				confidence += (2^(startVal - 6);
			}
			else if (startVal < 2)
			{
				confidence -= (2-startVal)*confidence;
			}
			bettingPhase();
		}
		
		// flop
		else if(game.cardsPlayed == 3)
		{
			myHand[2] = game.cards[0];
			myHand[3] = game.cards[1];
			myHand[4] = game.cards[2];			
			checkCurrentValue(3,2);
		}
		
		// 4th card
		else if(game.cardsPlayed == 4)
		{
			myHand = bestHand(myHand, game.cards[3]);
			// recalculate confidence fn goes here
					
			
		}
		
		// river
		else
		{
			myHand = bestHand(myHand, game.cards[4]);
			// recalculate confidence fn goes here	
		}
		
	}

	// determines the perceived value of the (starting cards
	// max is 5, min is 0
	private float startingHandValue()
	{
		float value = 0.0;
		Card high, low;
		char diff;
		if(a.value == b.value)
		{
			handValueIndex = 1;
			value += 2.0;
			// 0 = Two, 1 = Three, and so on
			char cnt = 2;

			// if pocket aces, this goes up by 3
			while(cnt < a.value)
			{
				value += 0.25;
				cnt++;
			}
		}
		else if (a.value > b.value)
		{
			high = a;
			low = b;
		}
		else
		{
			high = b;
			low = a;
		}
		diff = high.value - low.value;
		diff2 = low.value - high.value;
		if(diff < 5)
		{
			value += 2^(1-diff) + 1^(12 - high.value) + 1^(11-low.value);
		} 
		else if (diff2 + 13 < 5)
		{
			value += 2^(1-diff) + 1^(12 - high.value) + 1^(11-low.value);
		}
		if(a.suit == b.suit)
		{
			value += 0.5;
		}
		return value;
	}
	
	
	private void checkCurrentValue(char cardsDealt, char handIndex)
	{
		// recalculate confidence based off of all possible cards to come
	}

	// when a new card is given to the ai when the Turn and River cards are dealt
	private Card [] bestHand (Card [] current, Card next)
	{
		// get whatever our hands score is going before considering the new cards
		int currScore = calculator.score(current);

		for(char i = 0; i < 3; i++)
		{
			Card [] temp = current;
			temp[i+2] = next;
			
			// if the score of adding the new card to this slot is better than the
			// old hand then this is the new best hand we have currently
			int tempScore = calculator.score(temp);
			if(tempScore > currScore)
			{
				currScore = tempScore;
				current = temp;
			}
		}
		return current;
	}
	
	private void bettingPhase()
	{
		if(bet < game.bet)
		{
			if(confidence > betConfidenceThreshold)
			{
				if(game.bet >= 0.5 * chips && confidence < 0.85)
				{
					fold();
				}
				else
				{
					call();
				}
			}
		}
		else
		{
			if(confidence > betConfidenceThreshold)
			{
				if(confidence < 0.95)
				{
					if(1.5 * bet < 0.5 * chips)
					{
						raise(1.5 * bet);
					}
					else if (chips < 10)
					{
						raise(chips);
					}
				}
				else
				{
					raise(chips);
				}
			}
		}
	}

	// just turn the numbers passed in into Enums
	private Suit family(int x)
	{
		switch(x)
		{
			case 0: return CLUBS;
			case 1: return HEARTS;
			case 2: return DIAMONDS;
			default: return SPADES;
		}
	}
}
