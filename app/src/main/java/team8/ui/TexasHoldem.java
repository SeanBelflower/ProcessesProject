package team8.ui;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

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
    private int maxContribution = 0;
    private int playerIndex = 0; // Keeps track of the current player
    private int dealerIndex = 0; // Keeps track of the dealer
    private Deck deck;
    private int currentRound;

    final int USER_ID = 0;
    final int BOT1_ID = 1;
    final int BOT2_ID = 2;
    final int BOT3_ID = 3;
    final int BOT4_ID = 4;


    //main
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texas_holdem);

        thread = new Handler(Looper.getMainLooper());

        gamePlay();
        showPlayers(Integer.parseInt(getIntent().getStringExtra("numBots")));
        //do not write anything under this line, gamePlay() starts a thread which must be the only remaining execution in this program
    }

    //--------GAME--------

    // Must be called after preFlop()
    public void gamePlay()
    {
        currentRound = 0;
        preFlop();
        simulateTurns();
        //do not write anything under this line, simulateTurns() starts a thread which must be the only remaining execution in this program
    }

    // Sets up the game including picking the blinds, dealers, creating the deck, etc.
    public void preFlop()
    {
        //DEBUG
        Log.w("GAME_DEBUG", "--------PREFLOP--------");

        int startingChips = Integer.parseInt(getIntent().getStringExtra("startingChips"));

        deck = new Deck();
        deck.shuffle();

        numPlayers = Integer.parseInt(getIntent().getStringExtra("numBots")) + 1;
        this.players = new Player[numPlayers];

        for(int i = 0; i < numPlayers;i++)
        {
            players[i] = new Player(i, startingChips);
        }

        // New game pot gets reset
        updatePot(0);

        // picking the blinds and dealer
        players[dealerIndex % this.numPlayers].setDealer();
        players[dealerIndex + 1 % this.numPlayers].setSmallBlind();
        players[dealerIndex + 2 % this.numPlayers].setBigBlind();

        // Add two cards to each players hand
        for(int i = 0; i < numPlayers;i++)
        {
            players[i].addToHand(deck.getCard(2));
        }

        //show user's hand
        updatePlayerCards(players[USER_ID].getHand().get(0), players[USER_ID].getHand().get(1));

        // Add 3 cards to the table, only visible after the first round
        for(int i = 0; i < 3;i++)
        {
            cardsOnTable.add(deck.getCard());
        }

        dealer = players[dealerIndex % this.numPlayers];

        // Must make the initial bet
        currentPlayer = smallBlind = players[dealerIndex + 1 % this.numPlayers];

        // Must bet more than the small blind (I think its double)
        currentPlayer = bigBlind = players[dealerIndex + 2 % this.numPlayers];

        // playerIndex points to the current player, currently the player after the BigBlind
        playerIndex = dealerIndex + 3;

        dealerIndex++;

    }

    //flop logic and starts player turns
    public void flop()
    {
        //DEBUG
        Log.w("GAME_DEBUG", "--------FLOP--------");

        //show the the first 3 cards
        updateCommunityCards(cardsOnTable.get(0), cardsOnTable.get(1), cardsOnTable.get(2), null, null);

        maxContribution = -1;
        playerIndex = dealerIndex + 1 % numPlayers;
        resetContributions();
        thread.postDelayed(new Runnable(){
            public void run()
            {
                simulateTurns();
            }}, 5000);
    }

    //turn logic amd starts player turns
    public void turn()
    {
        //DEBUG
        Log.w("GAME_DEBUG", "--------TURN--------");

        cardsOnTable.add(deck.getCard());

        //show 4th card
        updateCommunityCards(cardsOnTable.get(0), cardsOnTable.get(1), cardsOnTable.get(2), cardsOnTable.get(3), null);

        //if all-in or all but one -> show cards
        // New round reset maxContribution and start at smallBlind again
        maxContribution = -1;
        playerIndex = dealerIndex + 1 % numPlayers;

        //start turns
        thread.postDelayed(new Runnable(){
            public void run()
            {
                simulateTurns();
            }}, 5000);
    }

    //river logic and starts player turns
    public void river()
    {
        //DEBUG
        Log.w("GAME_DEBUG", "--------RIVER--------");

        cardsOnTable.add(deck.getCard());

        //show 4th card
        updateCommunityCards(cardsOnTable.get(0), cardsOnTable.get(1), cardsOnTable.get(2), cardsOnTable.get(3), cardsOnTable.get(4));

        //if all-in or all but one -> show cards
        // New round reset maxContribution and start at smallBlind again
        maxContribution = -1;
        playerIndex = dealerIndex + 1 % numPlayers;

        //start turns
        thread.postDelayed(new Runnable(){
            public void run()
            {
                simulateTurns();
            }}, 5000);
    }

    public void simulateTurns()
    {
        currentPlayer = players[playerIndex % numPlayers];

        if(currentPlayer.getPlayerID() == USER_ID) //user
        {
            if(!currentPlayer.hasFolded())
                showUserOptions(currentRound);
            else
            {
                playerIndex++;
                simulateTurns();
            }
        }
        else
        {
            hideUserOptions();

            // Give players the option to raise, call, fold, checking (dumb for now, needs AI and user input)
            // currentPlayer.getPlayerID() gets the current ID of the player
            int randAction = (int) (Math.random() * 4);
            String action = "";
            switch (randAction)
            {
                case 0:
                    action = "Fold";
                    Log.w("GAME_DEBUG", "Round: " + currentRound + " Bot: " + currentPlayer.getPlayerID() + " action: " + action + " pot: " + pot);
                    showPlayerAction(currentPlayer.getPlayerID(), action);
                    fold();
                    break;
                case 1:
                    action = "Call";
                    Log.w("GAME_DEBUG", "Round: " + currentRound + " Bot: " + currentPlayer.getPlayerID() + " action: " + action + " pot: " + pot);
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
                    action = "Raise: " + (maxContribution + 10);
                    Log.w("GAME_DEBUG", "Round: " + currentRound + " Bot: " + currentPlayer.getPlayerID() + " action: " + action + " pot: " + pot);
                    showPlayerAction(currentPlayer.getPlayerID(), action);
                    raise(maxContribution + 10);
                    break;
            }

            thread.postDelayed(new Runnable(){
                public void run()
                {
                    Log.w("GAME_DEBUG", "Bets Equal: " + betsEqual());
                    if(!betsEqual())
                        simulateTurns();
                    else
                    {
                        currentRound++;
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
                        }
                    }
                }}, 5000);
        }
    }

    //returns whether user needs to contribute more, or if they can't, or 1 for success
    public int raise(int value)
    {
        if(value < maxContribution)
        {
            // Enter a higher amount than maxContribution
            return 0;
        }
        else if(!currentPlayer.raise(value)) //not enough funds
        {
            return -1;
        }
        else
        {
            maxContribution = value;
            updatePot(value);
            updatePlayerChips(currentPlayer.getPlayerID());
        }

        playerIndex++;

        return 1; //raise completed
    }

    public int call()
    {

        if(!currentPlayer.call(maxContribution))
        {
            // print call was not possible
            return 0;
        }
        updatePot(maxContribution);
        updatePlayerChips(currentPlayer.getPlayerID());
        playerIndex++;

        return 1;
    }

    public void fold()
    {
        currentPlayer.fold();
        playerIndex++;
    }

    public void bet(int value)
    {
        // get value only should be visible at the start of a round
        if(!currentPlayer.bet(value)){
            // print insuffcient funds
            return;
        }
        else
        {
            maxContribution = value;
            updatePot(value);
        }
        playerIndex++;
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

    //----------UI----------

    //shows bot cards ands chips, as well as user chips
    //called by preFlop()
    public void showPlayers(int numBots)
    {
        switch(numBots)
        {
            case 4:
                findViewById(R.id.b4Cards).setVisibility(View.VISIBLE);
                updatePlayerChips(BOT4_ID);
            case 3:
                findViewById(R.id.b3Cards).setVisibility(View.VISIBLE);
                updatePlayerChips(BOT3_ID);
            case 2:
                findViewById(R.id.b2Cards).setVisibility(View.VISIBLE);
                updatePlayerChips(BOT2_ID);
            case 1:
                findViewById(R.id.b1Cards).setVisibility(View.VISIBLE);
                updatePlayerChips(BOT1_ID);
            default:
                updatePlayerChips(USER_ID);

        }
    }

    //show user their options based on int round
    public void showUserOptions(int round)
    {
        Button raiseButton = (Button)findViewById(R.id.raise);
        Button foldButton = (Button)findViewById(R.id.fold);
        Button checkButton = (Button)findViewById(R.id.check);
        Button callButton = (Button)findViewById(R.id.call);
        Button betButton = (Button)findViewById(R.id.bet);
        round = 0;
        switch(round)
        {
            case 0:
                raiseButton.setVisibility(View.VISIBLE);
                foldButton.setVisibility(View.VISIBLE);
                callButton.setVisibility(View.VISIBLE);
                break;
            case 1:

                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    //hide user options
    public void hideUserOptions()
    {
        Button raiseButton = (Button)findViewById(R.id.raise);
        Button foldButton = (Button)findViewById(R.id.fold);
        Button checkButton = (Button)findViewById(R.id.check);
        Button callButton = (Button)findViewById(R.id.call);

        raiseButton.setVisibility(View.INVISIBLE);
        foldButton.setVisibility(View.INVISIBLE);
        checkButton.setVisibility(View.INVISIBLE);
        callButton.setVisibility(View.INVISIBLE);
    }

    //updates the action of player with playerID to action
    public void showPlayerAction(int playerID, String action)
    {
        switch(playerID)
        {
            case 0:
                TextView userAction = (TextView)findViewById(R.id.userAction);
                userAction.setText(action);
                userAction.setVisibility(View.VISIBLE);
                break;
            case 1:
                TextView bot1Action = (TextView)findViewById(R.id.b1Action);
                bot1Action.setText(action);
                bot1Action.setVisibility(View.VISIBLE);
                break;
            case 2:
                TextView bot2Action = (TextView)findViewById(R.id.b2Action);
                bot2Action.setText(action);
                bot2Action.setVisibility(View.VISIBLE);
                break;
            case 3:
                TextView bot3Action = (TextView)findViewById(R.id.b3Action);
                bot3Action.setText(action);
                bot3Action.setVisibility(View.VISIBLE);
                break;
            case 4:
                TextView bot4Action = (TextView)findViewById(R.id.b4Action);
                bot4Action.setText(action);
                bot4Action.setVisibility(View.VISIBLE);
                break;
        }
    }

    //updates the user's cards
    public void updatePlayerCards(Card card1, Card card2)
    {
        ImageView card1View = (ImageView)findViewById(R.id.card1);
        card1View.setImageResource(getResources().getIdentifier(card1.getDrawableSource(), null, getPackageName()));

        ImageView card2View = (ImageView)findViewById(R.id.card2);
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
        if(card4 == null)
        {
            //commCard4View.setVisibility(View.INVISIBLE);
        }
        else
        {
            commCard4View.setImageResource(getResources().getIdentifier(card4.getDrawableSource(), null, getPackageName()));
            commCard4View.setVisibility(View.VISIBLE);
        }

        ImageView commCard5View = (ImageView)findViewById(R.id.commCard5);
        if(card5 == null)
        {
            //commCard5View.setVisibility(View.INVISIBLE);
        }
        else
        {
            commCard5View.setImageResource(getResources().getIdentifier(card5.getDrawableSource(), null, getPackageName()));
            commCard5View.setVisibility(View.VISIBLE);
        }
    }

    //used to update all player chips
    public void updatePlayerChips(int playerID)
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
                TextView bot0Name = (TextView)findViewById(R.id.player);
                bot0Name.setText("Player: " + players[0].getChipStack());
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
        popupWindow.showAtLocation((RelativeLayout)findViewById(R.id.layout), Gravity.CENTER, 0, 0);

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
                    int result = raise(raiseAmount);
                    if(result == 1)
                    {
                        popupWindow.dismiss();

                        //DEBUG
                        Log.w("GAME_DEBUG", "Round: " + currentRound + " User: 0" + " action: Raise pot: " + pot);

                        hideUserOptions();
                        showPlayerAction(0, "Raise: " + raiseAmount);
                        thread.postDelayed(new Runnable(){
                            public void run()
                            {
                                simulateTurns();
                            }}, 5000);
                    }
                    else
                    {
                        if(result == 0)
                            warning.setText("Must match or exceed max contribution.");
                        else
                            warning.setText("Not enough chips.");
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
        popupWindow.showAtLocation((RelativeLayout)findViewById(R.id.layout), Gravity.CENTER, 0, 0);

        Button yesOpt = (Button)popupView.findViewById(R.id.yes);
        yesOpt.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                fold();
                popupWindow.dismiss();

                //DEBUG
                Log.w("GAME_DEBUG", "Round: " + currentRound + " User action: Fold pot: " + pot);

                hideUserOptions();
                showPlayerAction(0, "Fold");
                thread.postDelayed(new Runnable(){
                    public void run()
                    {
                        simulateTurns();
                    }}, 5000);
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
        popupWindow.showAtLocation((RelativeLayout)findViewById(R.id.layout), Gravity.CENTER, 0, 0);

        final Button yesOpt = (Button)popupView.findViewById(R.id.yes);
        final Button noOpt = (Button)popupView.findViewById(R.id.no);

        yesOpt.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                int result = call();

                if(result == 1)
                {
                    popupWindow.dismiss();

                    //DEBUG
                    Log.w("GAME_DEBUG", "Round: " + currentRound + " User action: Call pot: " + pot);

                    hideUserOptions();
                    showPlayerAction(0, "Call");
                    thread.postDelayed(new Runnable(){
                        public void run()
                        {
                            simulateTurns();
                        }}, 5000);
                }
                else
                {
                    final TextView warning = (TextView)popupView.findViewById(R.id.warning);
                    warning.setVisibility(View.VISIBLE);
                    yesOpt.setVisibility(View.GONE);
                    noOpt.setVisibility(View.GONE);

                    final Button ok = (Button)popupView.findViewById(R.id.ok);
                    final TextView callPrompt = (TextView)popupView.findViewById(R.id.callPrompt);
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
        popupWindow.showAtLocation((RelativeLayout)findViewById(R.id.layout), Gravity.CENTER, 0, 0);

        Button yesOpt = (Button)popupView.findViewById(R.id.yes);
        yesOpt.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v)
            {
                check();
                popupWindow.dismiss();

                //DEBUG
                Log.w("GAME_DEBUG", "Round: " + currentRound + "User action: Check pot: " + pot);

                hideUserOptions();
                showPlayerAction(0, "Check");
                thread.postDelayed(new Runnable(){
                    public void run()
                    {
                        simulateTurns();
                    }}, 5000);
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
            //when non-matching card is encountered, decide rank
            if(hand[i].getValue() != valueCheck || i == 5)
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

}
