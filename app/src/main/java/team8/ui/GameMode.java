package team8.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class GameMode extends AppCompatActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);
    }

    public void goToOptions(View view)
    {
        String mode = getIntent().getStringExtra("mode");

        if(mode.equals("Offline"))
        {
            Intent intent = new Intent(GameMode.this, OfflineOptions.class);

            intent.putExtra("mode", mode);
            intent.putExtra("username", getIntent().getStringExtra("username"));

            if(view.getId() == R.id.TexasHoldEm)
            {
                intent.putExtra("style", "texas");
            }
            else
            {
                intent.putExtra("style", "five");
            }

            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(GameMode.this, OnlineOptions.class);

            intent.putExtra("mode", mode);

            if(view.getId() == R.id.TexasHoldEm)
            {
                intent.putExtra("style", "texas");
            }
            else
            {
                intent.putExtra("style", "five");
            }

            startActivity(intent);
        }
    }
}
