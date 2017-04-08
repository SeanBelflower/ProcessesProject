package team8.ui;

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


    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texas_holdem);

       /* boolean PlayGame = true;
        while(PlayGame)
        {
            Deck deck = new Deck();
            deck.shuffle();
            setUp();
            gamePlay();
            // Finds the winning had and prints the winner
            // CalculateWinner();

            // Ask if the player wants to continue Playing
            // PlayGame = their response
        }*/

        setUp();
        updatePlayerCards(deck.getCard(), deck.getCard());
        updateCommunityCards(deck.getCard(), deck.getCard(), deck.getCard(), deck.getCard(), null);
        showPlayers(Integer.parseInt(getIntent().getStringExtra("numBots")));
    }

  /*  public TexasHoldEm(int numPlayers)
    {
        if(numPlayers < 3)
        {
            // Print something saying you need 3 players, dealer, SmallBlind, BigBlind
            return;
        }
        this.numPlayers = numPlayers;
        this.players = new Player[numPlayers];
        for(int i = 0; i < numPlayers;i++)
        {
            players[i] = new Player(i);
        }
    }*/

    // Must be called after setUp()
    public void gamePlay()
    {
        for(int i = 0; i < 4; i++)
        {
            if(i == 0)
            {
                // PreFlop, all of this is taken care of in setUp()
            }
            if(i == 1)
            {
                // Flop, Make cards on table visible, already added with setUp()

                // New round reset maxContribution and start at smallBlind again
                // -1 to make sure player contribution does not equal maxContribution at 0
                maxContribution = -1;
                playerIndex = dealerIndex + 1 % numPlayers;
                // smallBlind makes initial bet

            }
            if(i > 1)
            {
                // The Turn and The River

                // Add a card to cards on table
                cardsOnTable.add(deck.getCard());

                // New round reset maxContribution and start at smallBlind again
                maxContribution = -1;
                playerIndex = dealerIndex + 1 % numPlayers;
                // smallBlind makes initial bet

            }
            while(!betsEqual())
            {
                currentPlayer = players[playerIndex % numPlayers];
                // Give players the option to raise, call, fold, checking
                // currentPlayer.getPlayerID() gets the current ID of the player
            }
        }

    }

    //adds value to pot
    public void updatePot(int value)
    {
        pot += value;
        TextView potText = (TextView)findViewById(R.id.potText);
        potText.setText("" + pot);
    }

    //returns
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
        }

        playerIndex++;

        return 1;
    }

    public void call(View view)
    {

        if(!currentPlayer.call(maxContribution)){
            // print call was not possible
            return;
        }
        updatePot(maxContribution);
        playerIndex++;
    }

    public void fold(View view)
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
        else{
            maxContribution = value;
            updatePot(value);
        }
        playerIndex++;
    }

    public void check(View view)
    {
        // Should only be visible in rounds 2,3,4 not 1
        playerIndex++;
        currentPlayer = players[playerIndex % numPlayers];

    }

    // Sets up the game including picking the blinds, dealers, creating the deck, etc.
    public void setUp()
    {
        deck = new Deck();
        deck.shuffle();
        numPlayers = Integer.parseInt(getIntent().getStringExtra("numBots")) + 1;
        this.players = new Player[numPlayers];
        for(int i = 0; i < numPlayers;i++)
        {
            players[i] = new Player(i);
        }
        // New game pot gets reset
        updatePot(0);
        // picking the blinds and dealer
        players[dealerIndex % this.numPlayers].setDealer();
        players[dealerIndex + 1 % this.numPlayers].setSmallBlind();
        players[dealerIndex + 2 % this.numPlayers].setBigBlind();
        // Add two cards to each players hand
        for(int i = 0; i < numPlayers;i++){
            players[i].addToHand(deck.getCard(2));
        }
        // Add 3 cards to the table, only visible after the first round
        for(int i = 0; i < 3;i++){
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
    // checks to see if all the bets made by the current players is equal
    public boolean betsEqual()
    {
        boolean equal = true;
        for(int i = 0; i < this.numPlayers;i++)
        {
            equal &= ((players[i].getContribution() == this.maxContribution) ||
                    players[i].hasFolded());
        }
        return equal;
    }

    // Resets the players contributions
    public void resetContributions()
    {

        for(int i = 0; i < numPlayers;i++){
            players[i].resetContribution();
        }
    }

    public void showPlayers(int numBots)
    {
        switch(numBots)
        {
            case 4:
                findViewById(R.id.layoutB4).setVisibility(View.VISIBLE);
                TextView bot4Name = (TextView)findViewById(R.id.bot4Name);
                bot4Name.setText(getIntent().getStringExtra("name4"));
            case 3:
                findViewById(R.id.layoutB3).setVisibility(View.VISIBLE);
                TextView bot3Name = (TextView)findViewById(R.id.bot3Name);
                bot3Name.setText(getIntent().getStringExtra("name3"));
            case 2:
                findViewById(R.id.layoutB2).setVisibility(View.VISIBLE);
                TextView bot2Name = (TextView)findViewById(R.id.bot2Name);
                bot2Name.setText(getIntent().getStringExtra("name2"));
            case 1:
                findViewById(R.id.layoutB1).setVisibility(View.VISIBLE);
                TextView bot1Name = (TextView)findViewById(R.id.bot1Name);
                bot1Name.setText(getIntent().getStringExtra("name1"));
        }
    }

    public void updatePlayerCards(Card card1, Card card2)
    {
        ImageView card1View = (ImageView)findViewById(R.id.card1);
        card1View.setImageResource(getResources().getIdentifier(card1.getDrawableSource(), null, getPackageName()));

        ImageView card2View = (ImageView)findViewById(R.id.card2);
        card2View.setImageResource(getResources().getIdentifier(card2.getDrawableSource(), null, getPackageName()));
    }

    public void updateCommunityCards(Card card1, Card card2, Card card3, Card card4, Card card5)
    {
        ImageView commCard1View = (ImageView)findViewById(R.id.commCard1);
        commCard1View.setImageResource(getResources().getIdentifier(card1.getDrawableSource(), null, getPackageName()));

        ImageView commCard2View = (ImageView)findViewById(R.id.commCard2);
        commCard2View.setImageResource(getResources().getIdentifier(card2.getDrawableSource(), null, getPackageName()));

        ImageView commCard3View = (ImageView)findViewById(R.id.commCard3);
        commCard3View.setImageResource(getResources().getIdentifier(card3.getDrawableSource(), null, getPackageName()));

        ImageView commCard4View = (ImageView)findViewById(R.id.commCard4);
        if(card4 == null)
        {
            commCard4View.setVisibility(View.INVISIBLE);
        }
        else
        {
            commCard4View.setImageResource(getResources().getIdentifier(card4.getDrawableSource(), null, getPackageName()));
        }

        ImageView commCard5View = (ImageView)findViewById(R.id.commCard5);
        if(card5 == null)
        {
            commCard5View.setVisibility(View.INVISIBLE);
        }
        else
        {
            commCard5View.setImageResource(getResources().getIdentifier(card5.getDrawableSource(), null, getPackageName()));
        }
    }

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
                    int result = raise(Integer.parseInt(raiseText.getText().toString()));
                    if(result == 1)
                        popupWindow.dismiss();
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
}