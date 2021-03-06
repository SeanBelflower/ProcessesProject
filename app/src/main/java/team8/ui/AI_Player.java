package team8.ui;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;


public class AI_Player extends Player
{

    public String name;
    private int bet;
    private Random rand;
    private double confidence;
    private Card[] myHand = new Card[5];
    private Card a;
    private Card b;
    private int decision;
    private int boldness;
    private boolean newRound = true;

    //Needed for finding Suit constants
    private static final Suit SPADES = Suit.SPADES;
    private static final Suit DIAMONDS = Suit.DIAMONDS;
    private static final Suit HEARTS = Suit.HEARTS;
    private static final Suit CLUBS = Suit.CLUBS;

    private final double callConfidenceThreshold = 0.15;
    private final double betConfidenceThreshold = 0.35;


    public AI_Player(int playerID, int chips, String name)
    {
        super(playerID, chips);
        rand = new Random();
        this.name = name;
        boldness = rand.nextInt(9)+1;
        confidence = 0.05 * boldness;
        allCards = new ArrayList<>();
    }

    public int getRaiseAmount()
    {
        Log.w("GAME_DEBUG", "Bot's chipStack is " + chipStack + " and confidence is " + confidence);
        if(chipStack > 10 && confidence < .95)
        {
            return 10;
        }
        else
        {
            return chipStack;
        }
    }

    public void setHand(Card[] hand)
    {
        myHand = hand;
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
    public void observeHand(int cardsPlayed, int bet, boolean newRound)
    {
        if(this.hasFolded()) return;
        double score;
        if(a == null || b == null)
        {
            a = myHand[0] = allCards.get(0);
            b = myHand[1] = allCards.get(1);
        }

        // pre-flop
        if(cardsPlayed == 0 && newRound)
        {
            newRound = false;
            double startVal = startingHandValue();

            if(startVal > 1)
            {
                confidence += Math.pow(2, (startVal - 6));
            }
            else if (startVal < 1)
            {
                confidence *= Math.pow(2, (startVal - 2));
            }
        }

        // flop
        else if(cardsPlayed == 3)
        {
            Log.w("GAME_DEBUG", "In here");
            myHand[2] = allCards.get(2);
            myHand[3] = allCards.get(3);
            myHand[4] = allCards.get(4);
            score = hand.score(myHand);
            recalcConfidence(2, score, score);
        }

        // 4th card
        else if(cardsPlayed == 4)
        {
            score = hand.score(myHand);
            myHand = bestHand(allCards,6);
            double newScore = hand.score(myHand);
            recalcConfidence(1, score, newScore);
        }

        // river
        else if(cardsPlayed == 5)
        {
            score = hand.score(myHand);
            myHand = bestHand(allCards,7);
            double newScore = hand.score(myHand);
            recalcConfidence(0, score, newScore);
        }
        bettingPhase(bet);
    }

    // determines the perceived value of the (starting cards
    // max is 5, min is 0
    private double startingHandValue()
    {
        double value = 0;
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

        if(value < 0) value *= -1;

        while(value > 5)
        {
            value *= 0.5;
        }
        Log.w("GAME_DEBUG", "AI " + getPlayerID() + " Starting Hand is valued at " + value);
        return value;
    }


    private void recalcConfidence(int cardsLeft, double oldScore, double newScore)
    {
        Log.w("GAME_DEBUG", "oldScore: " + oldScore + " newScore: " + newScore);
        if(newScore > oldScore)
        {
            confidence += (newScore - oldScore) * (.000000025) * (3 - cardsLeft) * 3;
        }
        else
        {
            if(chipStack == 0) return;
            if(oldScore >= (getContribution()/chipStack * 0x954321))
            {
                confidence += oldScore * (.000000025) * (3-cardsLeft);
            }
            else
            {
                confidence -= (getContribution()/chipStack * 100) * .33 * (3-cardsLeft);
            }
        }

        if(confidence > 1.0)
        {
            confidence = 1.0;
        }
        if(confidence < 0)
        {
            confidence = 0;
        }
    }



    private void bettingPhase(int bet)
    {
        if(chipStack == 0)
        {
            decision = 2;
            return;
        }
        double score = 0;
        if(myHand[2] != null)
            score = scoreHand(myHand);
        else
            score = startingHandValue() * 10;

        // if the amount betted is less than the amount required to continue
        boolean hasMadeChoice = false;
        if(this.getContribution() < bet && confidence < betConfidenceThreshold)
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
                    confidence = (.8 * confidence);
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
                            confidence = (.8 * confidence);
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
            if(this.getContribution() < bet)
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
    public static double scoreHand (Card[] hand)
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

    //converts suit to value, name is a lie
    private static double suitToFloat (Suit suit)
    {
        double suitValue;
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
    private static Card[] sortHand (Card[] hand)
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
    private static double calculateScore (double handRank, Suit suit, double cardValue)
    {
        //the return value
        double handScore;

        //convert suit of most important card in hand to double, name is a lie
        double suitValue = suitToFloat (suit);

        //score the hand
        handScore = (handRank * 10) + (9 * (cardValue / 12)) + (suitValue / 4);

        //this ensures that the highest valued rank n is alsways lower than the lowest valued rank n+1
        if (suit == SPADES)
            handScore -= 0.01;

        //return the score
        return handScore;
    }

    public void observeHandFive(int bet)
    {
        for (int i = 0; i < 5; i++)
        {
            myHand[i] = allCards.get(i);
        }
        confidence = (hand.score(myHand) * 0.0000001) + boldness * 0.05;
        if(confidence > 1) confidence = 1;
        bettingPhase(bet);
    }

    public ArrayList<Card> replace()
    {
        ArrayList<Card> replace = new ArrayList<>();
        int score = hand.score(myHand);
        for(int j = 0; j < 5; j++)
        {
            Card [] temp = myHand;
            for(int i = 0; i < 52; i++)
            {
                Card temp2 = temp[j];
                temp[j] = new Card((i%13)+2, family(i/4));
                if(hand.score(temp) < score)
                {
                    if(!replace.contains(temp2))
                        replace.add(temp2);
                    break;
                }
            }
        }
        return replace;
    }
}
