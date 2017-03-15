import java.util.*;

public class AI_Player extends Player
{
	public String name;
	public short chips;
	public short bet;
	private Random rand;
	private float confidence;
	private Card hand[7]; 
	private char [] handValue = [0,1,2,3,4,5,6,7,8,9]
	private char handValueIndex = 0;
	private Game game;
	private Card a;
	private Card b;

	public AI_Player(String name, int chips, Game game)
	{
		this.name = name;
		this.chips = chips;
		this.boldness = rand.next(10)+1;
		this.confidence = 0.05*boldness;
		this.game = game;
	}

	// calculate confidence based on what cards have been played so far
	private void observeHand()
	{
		if(a == null || b == null)
		{
			a = game.draw();
			b = game.draw();
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

		}
		
		// flop
		else if(game.cardsPlayed == 3)
		{
			checkCurrentValue(3,2);
		}
		
		// 4th card
		else if(game.cardsPlayed == 4)
		{
			checkCurrentValue(1,5);
		}
		
		// river
		else
		{
			checkCurrentValue(1,6);
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
			value += 2.0
			// 0 = Two, 1 = Three, and so on
			char cnt = 0;

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
		diff = high.value - low.value+;
		diff2 = low.value - high.value;
		if(diff < 5)
		{
			value += 2^(1-diff) + 1^(12 - high.value) + 1^(11-low.value);
		}
		// 2 7 = 
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
	
	// determines value of cards given so far
	private void checkCurrentValue(char cardsDealt, char handIndex)
	{
		// get new cards
		for(char c = 0; c < cardsDealt; c++)
		{
			hand[handIndex+c] = game.cards[handIndex - 2]
		}
		handIndex += cardsDealt;
		
		// TODO: calculate where the handValueIndex is with cards dealt
		//	 and determine how likely it is to raise that index with
		//	 future cards
	}
}
