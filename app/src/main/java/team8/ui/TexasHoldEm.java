package team8.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class TexasHoldEm extends AppCompatActivity
{
    private Handler thread;

    private Player bigBlind;
    private Player smallBlind;
    private Player dealer;
    private int bigBlindValue;
    private Player currentPlayer;
    private ArrayList<Card> cardsOnTable = new ArrayList<Card>();
    private int numPlayers;
    public Player[] players;
    private int pot = 0;
    private int maxContribution = -1;
    private int playerIndex = 0; // Keeps track of the current player
    private int dealerIndex = 4; // Keeps track of the dealer, initialized to make user the smallBlind on start
    private Deck deck;
    private int currentRound;
    private boolean isPreFlop;

    private final int USER_ID = 0;
    private final int threadDelay = 2200;


    //main
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texas_holdem);

        thread = new Handler(Looper.getMainLooper());

        int startingChips = Integer.parseInt(getIntent().getStringExtra("startingChips"));
        int[] startingChipsStacks = {startingChips, startingChips, startingChips, startingChips, startingChips};

        gamePlay(startingChipsStacks);
        showPlayers(Integer.parseInt(getIntent().getStringExtra("numBots")));
        //do not write anything under this line, gamePlay() starts a thread which must be the only remaining execution in this program
    }

    //--------GAME--------

    // Must be called after preFlop()
    public void gamePlay(int[] chipStacks)
    {
        numPlayers = Integer.parseInt(getIntent().getStringExtra("numBots")) + 1;
        this.players = new Player[numPlayers];

        for(int i = 0; i < numPlayers; i++)
        {
            if(i == 0)
                players[i] = new Player(i, chipStacks[i]);
            else
            {
                String name = getIntent().getStringExtra("name" + i);
                players[i] = new AI_Player(i, chipStacks[i], name);
            }
        }

        currentRound = 0;

        preFlop();
        isPreFlop = true;

        thread.postDelayed(new Runnable(){

            public void run()
            {
                simulateTurns();
            }
        }, threadDelay);
        //do not write anything under this line, simulateTurns() starts a thread which must be the only remaining execution in this program
    }

    // Sets up the game including picking the blinds, dealers, creating the deck, etc.
    public void preFlop()
    {
        //DEBUG
        Log.w("GAME_DEBUG", "--------PREFLOP--------");

        deck = new Deck();
        deck.shuffle();

        // picking the blinds and dealer
        players[dealerIndex % this.numPlayers].setDealer();
        players[(dealerIndex + 1) % this.numPlayers].setSmallBlind();
        players[(dealerIndex + 2) % this.numPlayers].setBigBlind();

        // Add two cards to each players hand
        for(int i = 0; i < numPlayers;i++)
        {
            players[i].addToHand(deck.getCard(2));

            if(i > 0)
            {
                ((AI_Player)players[i]).addToCards(players[i].getHand().get(0));
                ((AI_Player)players[i]).addToCards(players[i].getHand().get(1));

                ((AI_Player)players[i]).observeHand(0, maxContribution, true);
            }
        }

        //show user's hand
        showUserCards(players[USER_ID].getHand().get(0), players[USER_ID].getHand().get(1));

        // Add 3 cards to the table, only visible after the first round
        for(int i = 0; i < 3;i++)
        {
            cardsOnTable.add(deck.getCard());

            for(int j = 1; j < numPlayers; j++)
            {
                ((AI_Player)players[j]).addToCards(cardsOnTable.get(i));
            }
        }

        dealer = players[dealerIndex % this.numPlayers];

        // Must make the initial bet
        currentPlayer = smallBlind = players[(dealerIndex + 1) % this.numPlayers];
        playerIndex = (dealerIndex + 1) % this.numPlayers;

        // Must bet more than the small blind (I think its double)
        currentPlayer = bigBlind = players[(dealerIndex + 2) % this.numPlayers];

        showBlinds(smallBlind.getPlayerID(), bigBlind.getPlayerID());

        // playerIndex points to the current player, currently the player after the BigBlind
        //playerIndex = dealerIndex + 1;

        dealerIndex++;
    }

    //flop logic and starts player turns
    public void flop()
    {
        //DEBUG
        Log.w("GAME_DEBUG", "--------FLOP--------");
        isPreFlop = false;

        //show the the first 3 cards
        updateCommunityCards(cardsOnTable.get(0), cardsOnTable.get(1), cardsOnTable.get(2), null, null);
        for(int i = 0; i < players.length; i++)
        {
            for(Card c : cardsOnTable)
                if(!players[i].allCards.contains(c))
                    players[i].allCards.add(c);
        }
        maxContribution = -1;
        playerIndex = smallBlind.getPlayerID();
        resetContributions();
        thread.postDelayed(new Runnable(){
            public void run()
            {
                simulateTurns();
            }}, threadDelay);
    }

    //turn logic and starts player turns
    public void turn()
    {
        //DEBUG
        Log.w("GAME_DEBUG", "--------TURN--------");

        cardsOnTable.add(deck.getCard());

        //show 4th card
        updateCommunityCards(cardsOnTable.get(0), cardsOnTable.get(1), cardsOnTable.get(2), cardsOnTable.get(3), null);

        for(int i = 0; i < players.length; i++)
        {
            for(Card c : cardsOnTable)
                if(!players[i].allCards.contains(c))
                    players[i].allCards.add(c);
        }

        //if all-in or all but one -> show cards
        // New round reset maxContribution and start at smallBlind again
        maxContribution = -1;
        playerIndex = smallBlind.getPlayerID();
        resetContributions();

        //start turns
        thread.postDelayed(new Runnable(){
            public void run()
            {
                simulateTurns();
            }}, threadDelay);
    }

    //river logic and starts player turns
    public void river()
    {
        //DEBUG
        Log.w("GAME_DEBUG", "--------RIVER--------");

        cardsOnTable.add(deck.getCard());

        //show 5th card
        updateCommunityCards(cardsOnTable.get(0), cardsOnTable.get(1), cardsOnTable.get(2), cardsOnTable.get(3), cardsOnTable.get(4));

        for(int i = 0; i < players.length; i++)
        {
            for(Card c : cardsOnTable)
                if(!players[i].allCards.contains(c))
                    players[i].allCards.add(c);
        }

        //if all-in or all but one -> show cards
        // New round reset maxContribution and start at smallBlind again
        maxContribution = -1;
        playerIndex = smallBlind.getPlayerID();
        resetContributions();
        //start turns
        thread.postDelayed(new Runnable(){
            public void run()
            {
                simulateTurns();
            }}, threadDelay);
    }

    public void simulateTurns()
    {
        currentPlayer = players[playerIndex % numPlayers];

        if(currentPlayer.getPlayerID() == USER_ID) //user's turn
        {
            if(!currentPlayer.hasFolded() && !allBotsFolded() && !betsEqual()) //show buttons if user has not folded
                showUserOptions(currentRound); //show buttons based on the current round
            else //keep the bots playing
            {
                playerIndex++;
                simulateTurns();
            }
        }
        else //bot turn
        {
            if(currentPlayer.hasFolded())
            {
                if(!betsEqual())
                {
                    playerIndex++;
                    simulateTurns(); //keep turns going
                }
                else
                {
                    Log.w("GAME_DEBUG", "Bets Equal");
                    currentRound++;
                    ((AI_Player)currentPlayer).observeHand(isPreFlop ? 0 : cardsOnTable.size(), maxContribution, true);
                    switch(currentRound)
                    {
                        case 1:
                            flop();
                            break;
                        case 2:
                            turn();
                            break;
                        case 3:
                            river();
                            break;
                        case 4:
                            continueGame();
                            break;
                    }
                }
            }
            else
            {
                hideUserOptions(); //hide user's buttons

                // Give players the option to raise, call, fold, checking (dumb for now, needs AI and user input)

                ((AI_Player)currentPlayer).observeHand(isPreFlop ? 0 : cardsOnTable.size(), maxContribution, false);
                int botAction = ((AI_Player)currentPlayer).getDecision();

                String action = "";

                if(currentPlayer.hasFolded())
                    botAction = 0; //keep folding
                else if((currentPlayer == smallBlind || currentPlayer == bigBlind) && currentPlayer.getContribution() < 1 && currentRound == 0)
                    botAction = 4; //force bet (5-10 style)

                switch (botAction)
                {
                    case 0:
                        action = "Fold";
                        Log.w("GAME_DEBUG", "Round: " + currentRound + " Bot: " + currentPlayer.getPlayerID() + " action: " + action + " pot: " + pot + " Contribution: " + currentPlayer);
                        showPlayerAction(currentPlayer.getPlayerID(), action);
                        fold();
                        break;
                    case 1:
                        action = "Call: " + maxContribution;
                        Log.w("GAME_DEBUG", "Round: " + currentRound + " Bot: " + currentPlayer.getPlayerID() + " action: " + action + " pot: " + (pot + maxContribution));
                        showPlayerAction(currentPlayer.getPlayerID(), action);
                        call();
                        break;
                    case 2:
                        action = "Check";
                        Log.w("GAME_DEBUG", "Round: " + currentRound + " Bot: " + currentPlayer.getPlayerID() + " action: " + action + " pot: " + pot);
                        showPlayerAction(currentPlayer.getPlayerID(), action);
                        check();
                        break;
                    case 3:
                        int raise = ((AI_Player) currentPlayer).getRaiseAmount();

                        if(maxContribution == -1 && raise != currentPlayer.getChipStack())
                            raise += 1;
                        if(raise == currentPlayer.getChipStack())
                            raise -= maxContribution;

                        action = "Raise: " + (raise + maxContribution);
                        Log.w("GAME_DEBUG", "Round: " + currentRound + " Bot: " + currentPlayer.getPlayerID() + " action: " + action + " pot: " + (pot + raise));
                        showPlayerAction(currentPlayer.getPlayerID(), action);
                        raise(raise);
                        break;
                    case 4:
                        int bet = 0;

                        //force 5-10 bets
                        if(currentPlayer == smallBlind)
                            bet = 5;
                        if(currentPlayer == bigBlind)
                            bet = 10;

                        action = "Bet: " + bet;
                        Log.w("GAME_DEBUG", "Round: " + currentRound + " Bot: " + currentPlayer.getPlayerID() + " action: " + action + " pot: " + (pot + bet));
                        showPlayerAction(currentPlayer.getPlayerID(), action);
                        bet(bet);
                        break;
                }

                logContributions();

                thread.postDelayed(new Runnable() {
                    public void run() {
                        if (!betsEqual())
                            simulateTurns();
                        else
                        {
                            Log.w("GAME_DEBUG", "Bets Equal");
                            currentRound++;
                            switch (currentRound) {
                                case 1:
                                    flop();
                                    break;
                                case 2:
                                    turn();
                                    break;
                                case 3:
                                    river();
                                    break;
                                case 4:
                                    continueGame();
                                    break;
                            }
                        }
                    }
                }, threadDelay);
            }
        }
    }

    public void continueGame()
    {
        int[] winners = determineWinners();

        hideBlinds(); //hide the old blinds

        cardsOnTable.clear(); //clear out the cards on the table
        hideCommunityCards(); //hide the community cards

        pot = 0; //reset the pot
        updatePot(0);

        openContinueWindow(winners); //start new game with current player chips
    }

    //returns whether user needs to contribute more, or if they can't, or 1 for success
    public int raise(int value)
    {
        // If raise is 10 and the players previous bet was 5, updatePotvalue should be 5
        int updatePotValue = currentPlayer.getContribution();
        if(value + maxContribution < maxContribution)
        {
            // Enter a higher amount than maxContribution
            return 0;
        }
        else if(!currentPlayer.raise(value + maxContribution)) //not enough funds
        {
            return -1;
        }
        else
        {
            maxContribution = value + maxContribution;
            updatePotValue = maxContribution - updatePotValue;
            updatePot(updatePotValue);
            updatePlayerInfo(currentPlayer.getPlayerID());
        }

        playerIndex++;

        return 1; //raise completed
    }

    public int call()
    {
        // If call is 10 and the players previous bet was 5, updatePotvalue should be 5
        int updatePotValue = maxContribution - currentPlayer.getContribution();
        if(!currentPlayer.call(maxContribution))
        {
            // print call was not possible
            return 0;
        }
        updatePot(updatePotValue);
        updatePlayerInfo(currentPlayer.getPlayerID());
        playerIndex++;

        return 1;
    }

    public void fold()
    {
        currentPlayer.fold();
        playerIndex++;
    }

    public int bet(int value)
    {
        // get value only should be visible at the start of a round
        if(!currentPlayer.bet(value))
        {
            // print insuffcient funds
            return 0;
        }
        else
        {
            maxContribution = value;
            updatePot(value);
            updatePlayerInfo(currentPlayer.getPlayerID());
        }
        playerIndex++;

        return 1;
    }

    public void check()
    {
        // Should only be visible in rounds 2,3,4 not 1

        playerIndex++;
        currentPlayer = players[playerIndex % numPlayers];

    }

    // checks to see if all the bets made by the current players is equal
    public boolean betsEqual()
    {
        boolean equal = true;

        int numFolded = 0;
        for(int i = 0; i < numPlayers; i++)
        {
            if(players[i].hasFolded())
                numFolded++;
        }

        if(numFolded == (numPlayers - 1))
            return true;

        for(int i = 0; i < this.numPlayers; i++)
        {
            equal &= ((players[i].getContribution() == this.maxContribution) ||
                    players[i].hasFolded());
        }
        return equal;
    }

    // Resets the players contributions
    public void resetContributions()
    {

        for(int i = 0; i < numPlayers;i++)
        {
            players[i].resetContribution();
        }
    }

    //returns array of all player chip stacks
    public int[] getAllChipStacks()
    {
        int[] chipStacks = new int[numPlayers];

        for(int i = 0; i < numPlayers; i++)
        {
            chipStacks[i] = players[i].getChipStack();
        }

        return chipStacks;
    }

    //Determines the winners
    public int[] determineWinners()
    {
        double[] scores = new double[numPlayers];
        int i = 0;

        for(Player player : players)
        {
            ArrayList<Card> allCards = new ArrayList<Card>();

            for(Card card : cardsOnTable)
                allCards.add(card);
            for(Card card : player.getHand())
                allCards.add(card);

            Card[] bestHand = player.bestHand(allCards, 7);
            double score = AI_Player.scoreHand(bestHand);

            if(player.hasFolded())
                score = 0;

            scores[i++] = score;
        }

        Arrays.sort(scores);

        Player[] sortedPlayers = new Player[numPlayers];

        for(Player player : players)
        {
            ArrayList<Card> allCards = new ArrayList<Card>();

            for(Card card : cardsOnTable)
                allCards.add(card);
            for(Card card : player.getHand())
                allCards.add(card);

            Card[] bestHand = player.bestHand(allCards, 7);
            double score = AI_Player.scoreHand(bestHand);

            if(player.hasFolded())
                score = 0;

            for(int j = 0; j < numPlayers; j++)
            {
                if(scores[j] == score && sortedPlayers[j] == null)
                {
                    sortedPlayers[j] = player;
                }
            }
        }

        int numWinners = 1;
        double winningScore = scores[numPlayers - 1]; //player with highest score

        i = numPlayers - 2; //start checking for ties at second to last player
        while(scores[i] == winningScore)
        {
            numWinners++;
        }

        int[] winners = new int[numWinners];
        int winnersIndex = 0;
        int winnings = pot/numWinners;

        Log.w("GAME_DEBUG", "Pot: " + pot + " winners: " + numWinners + " winnings: " + winnings);

        i = numPlayers - 1;
        do
        {
            winners[winnersIndex++] = sortedPlayers[i].getPlayerID();
            sortedPlayers[i].addToChipstack(winnings);
            updatePlayerInfo(sortedPlayers[i--].getPlayerID());
            Log.w("GAME_DEBUG", "Player " + sortedPlayers[i + 1].getPlayerID() + " chipstack " + sortedPlayers[i + 1].getChipStack());
            numWinners--;
        }while(numWinners > 0);

        for(int j = 0; j < numPlayers; j++)
        {
            Log.w("GAME_DEBUG", "Player " + sortedPlayers[j].getPlayerID() + " score: " + scores[j]);
        }

        return winners;
    }

    //returns whether or not all bots have folded
    public boolean allBotsFolded()
    {
        boolean allFolded = true;
        for(int i = 1; i < players.length; i++)
        {
            if(!players[i].hasFolded())
            {
                allFolded = false;
                break;
            }
        }

        return allFolded;
    }

    //----------UI----------

    //shows bot cards ands chips, as well as user chips
    //called by preFlop()
    public void showPlayers(int numBots)
    {
        switch(numBots)
        {
            case 4:
                findViewById(R.id.b4Cards).setVisibility(View.VISIBLE);
                updatePlayerInfo(4);
            case 3:
                findViewById(R.id.b3Cards).setVisibility(View.VISIBLE);
                updatePlayerInfo(3);
            case 2:
                findViewById(R.id.b2Cards).setVisibility(View.VISIBLE);
                updatePlayerInfo(2);
            case 1:
                findViewById(R.id.b1Cards).setVisibility(View.VISIBLE);
                updatePlayerInfo(1);
            default:
                updatePlayerInfo(USER_ID);

        }
    }

    //shows who the blinds are
    public void showBlinds(int smallBlindID, int bigBlindID)
    {
        TextView userBlind = (TextView)findViewById(R.id.userBlind);
        TextView b1Blind = (TextView)findViewById(R.id.b1Blind);
        TextView b2Blind = (TextView)findViewById(R.id.b2Blind);
        TextView b3Blind = (TextView)findViewById(R.id.b3Blind);
        TextView b4Blind = (TextView)findViewById(R.id.b4Blind);

        String smallBlindString = "SB";
        switch(smallBlindID)
        {
            case USER_ID:
                userBlind.setText(smallBlindString);
                break;
            case 1:
                b1Blind.setText(smallBlindString);
                break;
            case 2:
                b2Blind.setText(smallBlindString);
                break;
            case 3:
                b3Blind.setText(smallBlindString);
                break;
            case 4:
                b4Blind.setText(smallBlindString);
                break;
        }

        String bigBlindString = "BB";
        switch(bigBlindID)
        {
            case USER_ID:
                userBlind.setText(bigBlindString);
                break;
            case 1:
                b1Blind.setText(bigBlindString);
                break;
            case 2:
                b2Blind.setText(bigBlindString);
                break;
            case 3:
                b3Blind.setText(bigBlindString);
                break;
            case 4:
                b4Blind.setText(bigBlindString);
                break;
        }
    }

    //hides who the blinds are
    public void hideBlinds()
    {
        TextView userBlind = (TextView)findViewById(R.id.userBlind);
        TextView b1Blind = (TextView)findViewById(R.id.b1Blind);
        TextView b2Blind = (TextView)findViewById(R.id.b2Blind);
        TextView b3Blind = (TextView)findViewById(R.id.b3Blind);
        TextView b4Blind = (TextView)findViewById(R.id.b4Blind);

        userBlind.setText("");
        b1Blind.setText("");
        b2Blind.setText("");
        b3Blind.setText("");
        b4Blind.setText("");
    }

    //show user their options based on int round
    public void showUserOptions(int round)
    {
        Button raiseButton = (Button)findViewById(R.id.raise);
        Button foldButton = (Button)findViewById(R.id.fold);
        Button checkButton = (Button)findViewById(R.id.check);
        Button callButton = (Button)findViewById(R.id.call);
        Button betButton = (Button)findViewById(R.id.bet);

        if(round == 0)
        {
            if((currentPlayer == smallBlind && maxContribution < 1) || (currentPlayer == bigBlind && currentPlayer.getContribution() == 0))
            {
                betButton.setVisibility(View.VISIBLE);
            }
            else
            {
                raiseButton.setVisibility(View.VISIBLE);
                foldButton.setVisibility(View.VISIBLE);
                callButton.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            raiseButton.setVisibility(View.VISIBLE);
            foldButton.setVisibility(View.VISIBLE);
            if(maxContribution < 1)
            {
                checkButton.setVisibility(View.VISIBLE);
            }
            else
            {
                callButton.setVisibility(View.VISIBLE);
            }
        }

        if(betsEqual())
        {
            hideUserOptions();
            playerIndex++;
        }
    }

    //hide user options
    public void hideUserOptions()
    {
        Button raiseButton = (Button)findViewById(R.id.raise);
        Button foldButton = (Button)findViewById(R.id.fold);
        Button checkButton = (Button)findViewById(R.id.check);
        Button callButton = (Button)findViewById(R.id.call);
        Button betButton = (Button)findViewById(R.id.bet);

        raiseButton.setVisibility(View.INVISIBLE);
        foldButton.setVisibility(View.INVISIBLE);
        checkButton.setVisibility(View.INVISIBLE);
        callButton.setVisibility(View.INVISIBLE);
        betButton.setVisibility(View.INVISIBLE);
    }

    //updates the action of player with playerID to action
    public void showPlayerAction(int playerID, String action)
    {
        hidePlayerActions(false);

        TextView userAction = (TextView)findViewById(R.id.userAction);
        TextView bot1Action = (TextView)findViewById(R.id.b1Action);
        TextView bot2Action = (TextView)findViewById(R.id.b2Action);
        TextView bot3Action = (TextView)findViewById(R.id.b3Action);
        TextView bot4Action = (TextView)findViewById(R.id.b4Action);

        switch(playerID)
        {
            case USER_ID:
                userAction.setText(action);
                userAction.setVisibility(View.VISIBLE);
                break;
            case 1:
                bot1Action.setText(action);
                bot1Action.setVisibility(View.VISIBLE);
                break;
            case 2:
                bot2Action.setText(action);
                bot2Action.setVisibility(View.VISIBLE);
                break;
            case 3:
                bot3Action.setText(action);
                bot3Action.setVisibility(View.VISIBLE);
                break;
            case 4:
                bot4Action.setText(action);
                bot4Action.setVisibility(View.VISIBLE);
                break;
        }
    }

    //hides all player actions if they haven't fold or if it is a new round
    public void hidePlayerActions(boolean newRound)
    {
        TextView userAction = (TextView)findViewById(R.id.userAction);
        TextView bot1Action = (TextView)findViewById(R.id.b1Action);
        TextView bot2Action = (TextView)findViewById(R.id.b2Action);
        TextView bot3Action = (TextView)findViewById(R.id.b3Action);
        TextView bot4Action = (TextView)findViewById(R.id.b4Action);

        if(newRound)
        {
            userAction.setText("");
            bot1Action.setText("");
            bot2Action.setText("");
            bot3Action.setText("");
            bot4Action.setText("");

            return;
        }

        if(!players[USER_ID].hasFolded())
        {
            userAction.setText("");

        }

        if(!players[1].hasFolded())
        {
            bot1Action.setText("");
        }

        if(!players[2].hasFolded())
        {
            bot2Action.setText("");
        }

        if(numPlayers > 3)
            if(!players[3].hasFolded())
            {
                bot3Action.setText("");
            }

        if(numPlayers > 4)
            if(!players[4].hasFolded())
            {
                bot4Action.setText("");
            }
    }

    //shows the user's cards
    public void showUserCards(Card card1, Card card2)
    {
        ImageView card1View = (ImageView)findViewById(R.id.card3);
        card1View.setImageResource(getResources().getIdentifier(card1.getDrawableSource(), null, getPackageName()));

        ImageView card2View = (ImageView)findViewById(R.id.card4);
        card2View.setImageResource(getResources().getIdentifier(card2.getDrawableSource(), null, getPackageName()));
    }

    //updates the cards on the table
    public void updateCommunityCards(Card card1, Card card2, Card card3, Card card4, Card card5)
    {
        ImageView commCard1View = (ImageView)findViewById(R.id.commCard1);
        commCard1View.setImageResource(getResources().getIdentifier(card1.getDrawableSource(), null, getPackageName()));
        commCard1View.setVisibility(View.VISIBLE);

        ImageView commCard2View = (ImageView)findViewById(R.id.commCard2);
        commCard2View.setImageResource(getResources().getIdentifier(card2.getDrawableSource(), null, getPackageName()));
        commCard2View.setVisibility(View.VISIBLE);

        ImageView commCard3View = (ImageView)findViewById(R.id.commCard3);
        commCard3View.setImageResource(getResources().getIdentifier(card3.getDrawableSource(), null, getPackageName()));
        commCard3View.setVisibility(View.VISIBLE);

        ImageView commCard4View = (ImageView)findViewById(R.id.commCard4);
        if(card4 != null)
        {
            commCard4View.setImageResource(getResources().getIdentifier(card4.getDrawableSource(), null, getPackageName()));
            commCard4View.setVisibility(View.VISIBLE);
        }

        ImageView commCard5View = (ImageView)findViewById(R.id.commCard5);
        if(card5 != null)
        {
            commCard5View.setImageResource(getResources().getIdentifier(card5.getDrawableSource(), null, getPackageName()));
            commCard5View.setVisibility(View.VISIBLE);
        }
    }

    public void hideCommunityCards()
    {
        ImageView commCard1View = (ImageView)findViewById(R.id.commCard1);
        commCard1View.setVisibility(View.INVISIBLE);

        ImageView commCard2View = (ImageView)findViewById(R.id.commCard2);
        commCard2View.setVisibility(View.INVISIBLE);

        ImageView commCard3View = (ImageView)findViewById(R.id.commCard3);
        commCard3View.setVisibility(View.INVISIBLE);

        ImageView commCard4View = (ImageView)findViewById(R.id.commCard4);
        commCard4View.setVisibility(View.INVISIBLE);

        ImageView commCard5View = (ImageView)findViewById(R.id.commCard5);
        commCard5View.setVisibility(View.INVISIBLE);
    }

    //used to update all player chips
    public void updatePlayerInfo(int playerID)
    {
        switch(playerID)
        {
            case 4:
                TextView bot4Name = (TextView)findViewById(R.id.b4Info);
                bot4Name.setText(getIntent().getStringExtra("name4") + ": " + players[4].getChipStack());
                break;
            case 3:
                TextView bot3Name = (TextView)findViewById(R.id.b3Info);
                bot3Name.setText(getIntent().getStringExtra("name3") + ": " + players[3].getChipStack());
                break;
            case 2:
                TextView bot2Name = (TextView)findViewById(R.id.b2Info);
                bot2Name.setText(getIntent().getStringExtra("name2") + ": " + players[2].getChipStack());
                break;
            case 1:
                TextView bot1Name = (TextView)findViewById(R.id.b1Info);
                bot1Name.setText(getIntent().getStringExtra("name1") + ": " + players[1].getChipStack());
                break;
            case 0:
                TextView username = (TextView)findViewById(R.id.player);
                username.setText(getIntent().getStringExtra("username") + ": " + players[0].getChipStack());
                break;
        }
    }

    //adds value to pot, updates pot amount on table
    public void updatePot(int value)
    {
        pot += value;
        TextView potText = (TextView)findViewById(R.id.potText);
        potText.setText("" + pot);
    }

    //handles user Raise option
	public void openRaiseWindow(View view)
    {
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.raise_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.showAtLocation(findViewById(R.id.layout), Gravity.CENTER, 0, 0);


        final TextView warning = (TextView)popupView.findViewById(R.id.warning);
        warning.setText("");

        Button raiseSave = (Button)popupView.findViewById(R.id.raiseSave);
        final EditText raiseText = (EditText)popupView.findViewById(R.id.raiseText);
        raiseSave.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                if(!raiseText.getText().toString().isEmpty())
                {
                    int raiseAmount = Integer.parseInt(raiseText.getText().toString());

                    if(maxContribution == -1)
                        raiseAmount += 1;

                    if(raiseAmount > 0)
                    {

                        int result = raise(raiseAmount);
                        if(result == 1)
                        {
                            popupWindow.dismiss();

                            //DEBUG
                            Log.w("GAME_DEBUG", "Round: " + currentRound + " User " + " action: Raise pot: " + pot);
                            logContributions();

                            hideUserOptions();
                            showPlayerAction(0, "Raise: " + maxContribution);
                            thread.postDelayed(new Runnable(){
                                public void run()
                                {
                                    if (!betsEqual())
                                        simulateTurns();
                                    else
                                    {
                                        Log.w("GAME_DEBUG", "Bets Equal");
                                        currentRound++;
                                        switch (currentRound) {
                                            case 1:
                                                flop();
                                                break;
                                            case 2:
                                                turn();
                                                break;
                                            case 3:
                                                river();
                                                break;
                                            case 4:
                                                continueGame();
                                                break;
                                        }
                                    }
                                }}, threadDelay);
                        }
                        else
                        {
                            if(result == 0)
                                warning.setText("Must match or exceed max contribution.");
                            else
                                warning.setText("Not enough chips.");
                        }
                    }
                    else
                    {
                        warning.setText("Must raise more than 0 chips.");
                    }
                }
            }});
    }

    //handles user Fold option
    public void openFoldWindow(View view)
    {
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.fold_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(findViewById(R.id.layout), Gravity.CENTER, 0, 0);

        Button yesOpt = (Button)popupView.findViewById(R.id.yes);
        yesOpt.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                fold();
                popupWindow.dismiss();

                //DEBUG
                Log.w("GAME_DEBUG", "Round: " + currentRound + " User action: Fold pot: " + pot);
                logContributions();

                hideUserOptions();
                showPlayerAction(0, "Fold");
                thread.postDelayed(new Runnable(){
                    public void run()
                    {
                        if (!betsEqual())
                            simulateTurns();
                        else
                        {
                            Log.w("GAME_DEBUG", "Bets Equal");
                            currentRound++;
                            switch (currentRound) {
                                case 1:
                                    flop();
                                    break;
                                case 2:
                                    turn();
                                    break;
                                case 3:
                                    river();
                                    break;
                                case 4:
                                    continueGame();
                                    break;
                            }
                        }
                    }}, threadDelay);
            }
        });

        Button noOpt = (Button)popupView.findViewById(R.id.no);
        noOpt.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                popupWindow.dismiss();
            }
        });
    }

    //handles user Call option
    public void openCallWindow(View view)
    {
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.call_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(findViewById(R.id.layout), Gravity.CENTER, 0, 0);

        final Button yesOpt = (Button)popupView.findViewById(R.id.yes);
        final Button noOpt = (Button)popupView.findViewById(R.id.no);

        TextView prompt = (TextView)popupView.findViewById(R.id.prompt);
        prompt.setText("Call?");

        yesOpt.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                int result = call();

                if(result == 1)
                {
                    popupWindow.dismiss();

                    //DEBUG
                    Log.w("GAME_DEBUG", "Round: " + currentRound + " User action: Call pot: " + pot);
                    logContributions();

                    hideUserOptions();
                    showPlayerAction(0, "Call: " + maxContribution);
                    thread.postDelayed(new Runnable(){
                        public void run()
                        {
                            if (!betsEqual())
                                simulateTurns();
                            else
                            {
                                Log.w("GAME_DEBUG", "Bets Equal");
                                currentRound++;
                                switch (currentRound) {
                                    case 1:
                                        flop();
                                        break;
                                    case 2:
                                        turn();
                                        break;
                                    case 3:
                                        river();
                                        break;
                                    case 4:
                                        continueGame();
                                        break;
                                }
                            }
                        }}, threadDelay);
                }
                else
                {
                    final TextView warning = (TextView)popupView.findViewById(R.id.warning);
                    warning.setVisibility(View.VISIBLE);
                    yesOpt.setVisibility(View.GONE);
                    noOpt.setVisibility(View.GONE);

                    final Button ok = (Button)popupView.findViewById(R.id.ok);
                    final TextView callPrompt = (TextView)popupView.findViewById(R.id.prompt);
                    callPrompt.setVisibility(View.GONE);
                    ok.setVisibility(View.VISIBLE);
                    ok.setOnClickListener(new Button.OnClickListener(){
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            warning.setVisibility(View.GONE);
                            yesOpt.setVisibility(View.VISIBLE);
                            noOpt.setVisibility(View.VISIBLE);
                            ok.setVisibility(View.GONE);
                            callPrompt.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        noOpt.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                popupWindow.dismiss();
            }
        });
    }

    //handles user Check option
    public void openCheckWindow(View view)
    {
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.check_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(findViewById(R.id.layout), Gravity.CENTER, 0, 0);

        Button yesOpt = (Button)popupView.findViewById(R.id.yes);
        yesOpt.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                check();
                popupWindow.dismiss();

                //DEBUG
                Log.w("GAME_DEBUG", "Round: " + currentRound + " User action: Check pot: " + pot);
                logContributions();

                hideUserOptions();
                showPlayerAction(0, "Check");
                thread.postDelayed(new Runnable(){
                    public void run()
                    {
                        if (!betsEqual())
                            simulateTurns();
                        else
                        {
                            Log.w("GAME_DEBUG", "Bets Equal");
                            currentRound++;
                            switch (currentRound) {
                                case 1:
                                    flop();
                                    break;
                                case 2:
                                    turn();
                                    break;
                                case 3:
                                    river();
                                    break;
                                case 4:
                                    continueGame();
                                    break;
                            }
                        }
                    }}, threadDelay);
            }
        });

        Button noOpt = (Button)popupView.findViewById(R.id.no);
        noOpt.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                popupWindow.dismiss();
            }
        });
    }

    //handles user Raise option
    public void openBetWindow(View view)
    {
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.call_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(findViewById(R.id.layout), Gravity.CENTER, 0, 0);

        final Button yesOpt = (Button)popupView.findViewById(R.id.yes);
        final Button noOpt = (Button)popupView.findViewById(R.id.no);

        TextView prompt = (TextView)popupView.findViewById(R.id.prompt);
        prompt.setText("Bet?");

        yesOpt.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                int betAmount = 5;
                if(currentPlayer == bigBlind)
                    betAmount = 10;

                int result = bet(betAmount);

                if(result == 1)
                {
                    popupWindow.dismiss();

                    //DEBUG
                    Log.w("GAME_DEBUG", "Round: " + currentRound + " User action: Call pot: " + pot);
                    logContributions();

                    hideUserOptions();
                    showPlayerAction(0, "Bet: " + betAmount);
                    thread.postDelayed(new Runnable(){
                        public void run()
                        {
                            if (!betsEqual())
                                simulateTurns();
                            else
                            {
                                Log.w("GAME_DEBUG", "Bets Equal");
                                currentRound++;
                                switch (currentRound) {
                                    case 1:
                                        flop();
                                        break;
                                    case 2:
                                        turn();
                                        break;
                                    case 3:
                                        river();
                                        break;
                                    case 4:
                                        continueGame();
                                        break;
                                }
                            }
                        }}, threadDelay);
                }
                else
                {
                    final TextView warning = (TextView)popupView.findViewById(R.id.warning);
                    warning.setVisibility(View.VISIBLE);
                    yesOpt.setVisibility(View.GONE);
                    noOpt.setVisibility(View.GONE);

                    final Button ok = (Button)popupView.findViewById(R.id.ok);
                    final TextView callPrompt = (TextView)popupView.findViewById(R.id.prompt);
                    callPrompt.setVisibility(View.GONE);
                    ok.setVisibility(View.VISIBLE);
                    ok.setOnClickListener(new Button.OnClickListener(){
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            warning.setVisibility(View.GONE);
                            yesOpt.setVisibility(View.VISIBLE);
                            noOpt.setVisibility(View.VISIBLE);
                            ok.setVisibility(View.GONE);
                            callPrompt.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        noOpt.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                popupWindow.dismiss();
            }
        });
    }

    public void openContinueWindow(int[] winners)
    {
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.continue_game_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(findViewById(R.id.layout), Gravity.CENTER, 0, 0);

        String winnerText = "Winner(s): ";

        for(int num : winners)
        {
            if(num == 0)
            {
                winnerText += getIntent().getStringExtra("username") + ", ";
            }
            else
            {
                winnerText += getIntent().getStringExtra("name" + num) + ", ";
            }
        }

        winnerText = winnerText.substring(0, winnerText.length() - 2);

        TextView winnerTextView = (TextView)popupView.findViewById(R.id.winner);
        winnerTextView.setText(winnerText);

        Button continueButton = (Button)popupView.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                popupWindow.dismiss();

                hidePlayerActions(true); //hides player actions after user continues

                //DEBUG
                Log.w("GAME_DEBUG", "NEW GAME");

                thread.postDelayed(new Runnable(){
                    public void run()
                    {
                        gamePlay(getAllChipStacks());
                    }}, threadDelay);
            }
        });
    }

    //----------AI----------

    //ranks a hand
    public int rankHand (Card[] hand)
    {
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
        for(int i = 0; i < hand.length - 1; i++)
        {
            if(hand[i].getSuit() != hand[i+1].getSuit())
            {
                break;
            }
            flush = true;
        }

        //check for royal flush and straight flush
        if(flush && straight)
        {
            if(hand[0].getValue() == 8) //revisit
            {
                return 10;
            }
            else
            {
                return 9;
            }
        }

        //check for other hands
        int valueCheck = hand[0].getValue();
        int counter = 1;
        //one loop through hand
        for(int i = 1; i <= 5; i++)
        {
            if(i == 5)
            {
                switch (counter)
                {
                    case 2: if (three)
                        fullHouse = true;
                    else if (pair)
                        twoPair = true;
                    else
                        pair = true;
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
                        pair = true;
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
        //return rank of hand
        if (four)
            return 8;
        if (fullHouse)
            return 7;
        if (flush)
            return 6;
        if (straight)
            return 5;
        if (three)
            return 4;
        if (twoPair)
            return 3;
        if (pair)
            return 2;

        //lol loser
        return 1;
    }

    //ghetto bubblesort for Card
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

    //DEBUG functions
    public void logContributions()
    {
        if(numPlayers < 4)
        {
            Log.w("GAME_DEBUG", "Player Contributions: User: " + players[0].getContribution() + " 1: " + players[1].getContribution() + " 2: " + players[2].getContribution());
        }
        if(numPlayers == 4)
        {
            Log.w("GAME_DEBUG", "Player Contributions: User: " + players[0].getContribution() + " 1: " + players[1].getContribution() + " 2: " + players[2].getContribution() + " 3: " + players[3].getContribution());
        }
        if(numPlayers == 5)
        {
            Log.w("GAME_DEBUG", "Player Contributions: User: " + players[0].getContribution() + " 1: " + players[1].getContribution() + " 2: " + players[2].getContribution() + " 3: " + players[3].getContribution() + " 4: " + players[4].getContribution());

        }
        Log.w("GAME_DEBUG", "Max Contribution: " + maxContribution);

    }
}
