package team8.ui;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;


public class AI_Player extends Player
{

    public String name;
    public int bet;
    private Random rand;
    private float confidence;
    private Card[] myHand = new Card[5];
    private Card a;
    private Card b;
    private int decision;
    public int boldness;
    private boolean newRound = true;

    //Needed for finding Suit constants
    private final Suit SPADES = Suit.SPADES;
    private final Suit DIAMONDS = Suit.DIAMONDS;
    private final Suit HEARTS = Suit.HEARTS;
    private final Suit CLUBS = Suit.CLUBS;

    private final float callConfidenceThreshold = (float)0.4;
    private final float betConfidenceThreshold = (float)0.6;


    public AI_Player(int playerID, int chips, String name)
    {
        super(playerID, chips);
        rand = new Random();
        this.name = name;
        boldness = rand.nextInt(9)+1;
        confidence = 0;
        allCards = new ArrayList<Card>();
    }

    public int getRaiseAmount()
    {
        Log.w("GAME_DEBUG", "Bot's chipStack is " + chipStack + " and confidence is " + confidence);
        if(chipStack > 10 && confidence < .95)
        {
            if(bet * 1.5 < chipStack)
                return (int)(bet * 1.5);
            else
                return chipStack;
        }
        else
        {
            return chipStack;
        }
    }

    //adds cards to all cards
    public void addToCards(Card card)
    {
        allCards.add(card);
    }

    public int getDecision()
    {
        Log.w("GAME_DEBUG", "Bot: " + getPlayerID() + " Confidence: " + confidence + " Decision: " + decision + " Chips: " + chipStack);
        Log.w("GAME_DEBUG", "Bot: " + getPlayerID() + " cards: " + allCards.get(0).toString() + " " + allCards.get(1).toString());
        return decision;
    }

    // calculate confidence based on what cards have been played so far
    public void observeHand(int cardsPlayed, int bet, boolean newRound2)
    {
        float score;
        if(a == null || b == null)
        {
            a = myHand[0] = allCards.get(0);
            b = myHand[1] = allCards.get(1);
        }

        // pre-flop
        if(cardsPlayed == 0 && newRound)
        {
            newRound = false;
            float startVal = startingHandValue();


            if(startVal > 2)
            {
                confidence += Math.pow(2, (startVal - 6));
            }
            else if (startVal < 2)
            {
                confidence *= (float)Math.pow(2, (startVal - 2));
            }
        }

        // flop
        else if(cardsPlayed == 3)
        {

            myHand[2] = allCards.get(2);
            myHand[3] = allCards.get(3);
            myHand[4] = allCards.get(4);
            score = scoreHand(myHand);
            recalcConfidence(2, score, score);
        }

        // 4th card
        else if(cardsPlayed == 4)
        {
            score = scoreHand(myHand);
            myHand = bestHand(allCards,6);
            float newScore = scoreHand(myHand);
            recalcConfidence(1, score, newScore);
        }

        // river
        else
        {
            score = scoreHand(myHand);
            myHand = bestHand(allCards,7);
            float newScore = scoreHand(myHand);
            recalcConfidence(0, score, newScore);
        }
        bettingPhase(bet);
    }

    // determines the perceived value of the (starting cards
    // max is 5, min is 0
    private float startingHandValue()
    {
        float value = (float)0.0;
        Card high, low;
        int diff;
        int diff2;
        if(a.value == b.value)
        {
            value += 2.0;
            char cnt = 2;

            // if pocket aces, this goes up by 3
            while(cnt < a.value)
            {
                value += 0.25;
                cnt++;
            }
            high = a;
            low = high;
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
        diff = high.getValue() - low.getValue();
        diff2 = low.getValue() - high.getValue();
        if(diff < 5 && diff > 0)
        {
            value += Math.pow(2, 2 - diff) + Math.pow(1.5, high.value - 14) + Math.pow(1.5, high.value - 13);
        }
        else if (diff2 + 13 < 5 && diff > 0)
        {
            value += Math.pow(2, 2 - diff) + Math.pow(1.5, high.value - 14) + Math.pow(1.5, high.value - 13);
        }
        if(a.getSuit() == b.getSuit())
        {
            value += 0.5;
        }
        if(high.value > 9 && diff != 0)
        {
            value += 2^(high.value-15);
        }
        Log.w("GAME_DEBUG", "AI " + getPlayerID() + " Starting Hand is valued at " + value);
        return value;
    }


    private void recalcConfidence(int cardsLeft, float oldScore, float newScore)
    {
        if(newScore > oldScore)
        {
            confidence += (newScore - oldScore) * .01 * (3 - cardsLeft) * 3;
        }
        else
        {
            if(chipStack == 0) return;
            if(oldScore >= (float)(bet/chipStack * 100))
            {
                confidence += oldScore * .01;
            }
            else
            {
                confidence -= (float)(bet/chipStack * 100) * .33 * (3-cardsLeft);
            }
        }

        if(confidence > 1.0)
        {
            confidence = (float)1.0;
        }
        if(confidence < 0)
        {
            confidence = 0;
        }
    }

    // when a new card is given to the ai when the Turn and River cards are dealt
    private Card [] bestHand (ArrayList<Card> all, int cardsPlayed)
    {
        Card [] base = {allCards.get(0),allCards.get(1),allCards.get(2),allCards.get(3),allCards.get(4)};
        Card [] bestWithOne, bestWithTwo;
        bestWithOne = base;
        bestWithTwo = base;
        float score = scoreHand(base);
        for(Card c : all)
        {
            Log.w("GAME_DEBUG", c.toString());
        }
        float score2 = score;
        for(int i = 5; i < cardsPlayed; i++)
        {
            for(int j = 0; j < 5; j++)
            {
                Card [] temp = base;
                temp[j] = all.get(i);
                float tempScore = scoreHand(temp);
                if(tempScore > score)
                {
                    bestWithOne = temp;
                    score = tempScore;
                }
            }
        }
        if(cardsPlayed > 6)
        {
            Card[] base2 = {allCards.get(5),allCards.get(6),allCards.get(0),allCards.get(1),allCards.get(2)};
            for(int i = 3; i < cardsPlayed - 2; i++)
            {
                for(int j = 2; j < 5; j++)
                {
                    Card [] temp = base2;
                    temp[j] = all.get(i);
                    float tempScore = scoreHand(temp);
                    if(tempScore > score2)
                    {
                        bestWithTwo = temp;
                        score2 = tempScore;
                    }
                }
            }

            if (score == score2) return base;
            return (score > score2) ? bestWithOne : bestWithTwo;
        }
        return bestWithOne;
    }

    private void bettingPhase(int bet)
    {
        if(chipStack == 0)
        {
            decision = 2;
            return;
        }
        float score = 0;
        if(myHand[2] != null)
            score = scoreHand(myHand);
        else
            score = startingHandValue() * 10;

        // if the amount betted is less than the amount required to continue
        boolean hasMadeChoice = false;
        if(this.bet < bet && confidence < betConfidenceThreshold)
        {
            if(confidence > callConfidenceThreshold)
            {
                if(bet/chipStack > confidence)
                {
                    decision = 0;
                }
                else
                {
                    decision = 1;
                    confidence = (float) (.8 * confidence);
                }
                hasMadeChoice = true;
            }
            else
            {
                decision = 0;
            }
        }
        else
        {
            if(confidence > betConfidenceThreshold)
            {
                if(confidence < 0.95)
                {
                    if(1.5 * bet < score * (this.bet/chipStack + confidence/2))
                    {
                        if(1.5 * bet < chipStack)
                            decision = 3;
                        else
                            decision = 1;
                        hasMadeChoice = true;
                    }
                    else if(this.bet < bet)
                    {
                        if(bet/chipStack > confidence)
                        {
                            decision = 0;
                        }
                        else
                        {
                            decision = 1;
                            confidence = (float) (.8 * confidence);
                        }
                        hasMadeChoice = true;
                    }
                    else if (chipStack < 10)
                    {
                        decision = 3;
                        hasMadeChoice = true;
                    }
                }
                else
                {
                    decision = 3;
                    hasMadeChoice = true;
                }
            }
        }

        if(!hasMadeChoice)
        {
            if(this.bet < bet)
            {
                decision = 0;
                return;
            }
            decision = 2;
        }
    }

    // just turn the numbers passed in into Enums
    private Suit family(int x)
    {
        switch(x)
        {
            case 0: return CLUBS;
            case 1: return DIAMONDS;
            case 2: return HEARTS;
            default: return SPADES;
        }
    }
    //scores a hand
    public float scoreHand (Card[] hand)
    {
        int valueCheck;
        int counter;

        //primitive boolean defaults to false
        boolean flush = false, straight = false, pair = false, three = false, four = false, twoPair = false, fullHouse = false;

        //sort the hand to make it easier to rank
        hand = sortHand(hand);

        //check for straight
        for(int i = 1; i < 5; i++)
        {
            //can see wraparound straights
            if(hand[i].getValue() - 2 != hand[i-1].getValue() - 1 % 13)
            {
                break;
            }
            else if(i == 4)
            {
                straight = true;
            }
        }

        //check for flush
        for (int i=0; i<hand.length-1; i++)
        {
            if (hand[i].getSuit() != hand[i+1].getSuit())
            {
                break;
            }
            flush = true;
        }

        //check for royal flush and straight flush
        if(flush && straight)
        {
            //royal flush
            if(hand[0].getValue() == 8)
            {
                return calculateScore (10, hand[0].getSuit(), hand[4].getValue());
            }
            else
            {
                //straight flush
                return calculateScore (9, hand[0].getSuit(), hand[4].getValue());
            }
        }

        //check for other hands

        valueCheck = hand[0].getValue();
        counter = 1;
        //keep track of what pair we have
        int pairValue = 0;
        int twoPairValue;
        //one loop through hand
        for(int i = 1; i <= 5; i++)
        {
            if(i == 5)
            {
                switch (counter) {
                    case 2:
                        if (three)
                            fullHouse = true;
                        else if (pair)
                            twoPair = true;
                        else {
                            pair = true;
                            pairValue = valueCheck;
                        }
                        break;
                    case 3:
                        if (pair)
                            fullHouse = true;
                        else
                            three = true;
                        break;
                    case 4:
                        four = true;
                        break;
                    default:
                        break;
                }
                break;
            }
            //when non-matching card is encountered, decide rank
            if(hand[i].getValue() != valueCheck)
            {
                switch (counter)
                {
                    case 2: if (three)
                        fullHouse = true;
                    else if (pair)
                        twoPair = true;
                    else
                    {
                        pair = true;
                        pairValue = valueCheck;
                    }
                        break;
                    case 3: if (pair)
                        fullHouse = true;
                    else
                        three = true;
                        break;
                    case 4: four = true;
                        break;
                    default: break;
                }
                valueCheck = hand[i].getValue();
                counter = 1;
            }
            else counter++;
        }

        Card[] pairOfCards = new Card[2];
        //return score of the rest of the hands
        if (four)
            return calculateScore (8, SPADES, hand[1].getValue());
        if (fullHouse)
        {
            //determine which half of the hand is three of a kind
            if (hand[0].getValue() == hand[2].getValue())
            {
                return (7 * 10) + (9 * (hand[0].getValue() / 12)) +
                        (suitToFloat(hand[0].getSuit()) / 12) +
                        (suitToFloat(hand[1].getSuit()) / 12) +
                        (suitToFloat(hand[2].getSuit()) / 12);
            }
            else
            {
                return (7 * 10) + (9 * (hand[2].getValue() / 12)) +
                        (suitToFloat(hand[2].getSuit()) / 12) +
                        (suitToFloat(hand[3].getSuit()) / 12) +
                        (suitToFloat(hand[4].getSuit()) / 12);
            }
        }
        if (flush)
            return calculateScore (6, hand[0].getSuit(), hand[4].getValue());
        if (straight)
            return calculateScore (5, hand[4].getSuit(), hand[4].getValue());
        if (three)
        {
            //determine which half of the hand is three of a kind
            if (hand[0].getValue() == hand[2].getValue())
            {
                return (7 * 10) + (9 * (hand[0].getValue() / 12)) +
                        (suitToFloat(hand[0].getSuit()) / 12) +
                        (suitToFloat(hand[1].getSuit()) / 12) +
                        (suitToFloat(hand[2].getSuit()) / 12);
            }
            else
            {
                return (7 * 10) + (9 * (hand[2].getValue() / 12)) +
                        (suitToFloat(hand[2].getSuit()) / 12) +
                        (suitToFloat(hand[3].getSuit()) / 12) +
                        (suitToFloat(hand[4].getSuit()) / 12);
            }
        }
        if (twoPair)//3
        {
            if (hand[3].getValue() == hand[4].getValue())
            {
                return (7 * 10) + (9 * (hand[3].getValue() / 12)) +
                        (suitToFloat(hand[3].getSuit()) / 8) +
                        (suitToFloat(hand[4].getSuit()) / 8);
            }
            else
            {
                return (7 * 10) + (9 * (hand[2].getValue() / 12)) +
                        (suitToFloat(hand[2].getSuit()) / 8) +
                        (suitToFloat(hand[3].getSuit()) / 8);
            }

        }

        if (pair)
        {
            for (int i=0; i<hand.length; i++)
            {
                if (hand[i].getValue() == pairValue)
                {
                    pairOfCards[0] = hand[i];
                    pairOfCards[1] = hand[i+1];
                    break;
                }
            }
            return (7 * 10) + (9 * (pairOfCards[0].getValue() / 12)) +
                    (suitToFloat(pairOfCards[0].getSuit()) / 8) +
                    (suitToFloat(pairOfCards[1].getSuit()) / 8);
        }
        return calculateScore (1, hand[4].getSuit(), hand[4].getValue());
    }

    //converts suit to value
    float suitToFloat (Suit suit)
    {
        float suitValue;
        if (suit == CLUBS)
            suitValue = 1;
        else if (suit == DIAMONDS)
            suitValue = 2;
        else if (suit == HEARTS)
            suitValue = 3;
        else
            suitValue = 4;
        return suitValue;
    }

    //ghetto bubblesort
    public Card[] sortHand (Card[] hand)
    {
        int bound = 4;
        Card temp;
        while (bound>0)
        {
            for (int i=0; i<bound-1; i++)
            {
                if (hand[i].compareTo(hand[i+1]) > 0)
                {
                    temp = hand[i];
                    hand[i] = hand[i+1];
                    hand[i+1] = temp;
                }
            }
            bound--;
        }
        return hand;
    }

    //calculate the score of a given hand after finding its rank
    private float calculateScore (float handRank, Suit suit, float cardValue)
    {
        //the return value
        float handScore;

        //convert suit of most important card in hand to float
        float suitValue = suitToFloat (suit);

        //score the hand
        handScore = (handRank * 10) + (9 * (cardValue / 12)) + (suitValue / 4);

        //this ensures that the highest valued rank n is alsways lower than the lowest valued rank n+1
        if (suit == SPADES)
            handScore -= 0.01;

        //return the score
        return handScore;
    }
}