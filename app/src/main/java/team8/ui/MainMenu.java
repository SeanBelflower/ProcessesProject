package team8.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends AppCompatActivity
{

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void goToGameMode(View view)
    {
        String mode = "";
        switch(view.getId())
        {
            case R.id.offlineButton:
                mode = "Offline";
                break;
            case R.id.onlineButton:
                mode = "Online";
                break;
        }
        Intent intent = new Intent(MainMenu.this, GameMode.class);
        intent.putExtra("mode", mode);
        intent.putExtra("username", getIntent().getStringExtra("username"));
        startActivity(intent);
    }
}
