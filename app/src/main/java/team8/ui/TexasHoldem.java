package team8.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TexasHoldem extends AppCompatActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texas_holdem);

        Deck deck = new Deck();
        deck.shuffle();
        updatePlayerCards(deck.getCard(), deck.getCard());
        updateCommunityCards(deck.getCard(), deck.getCard(), deck.getCard(), deck.getCard(), null);
        showPlayers(Integer.parseInt(getIntent().getStringExtra("numBots")));
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


}
