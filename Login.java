package team8.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Login extends AppCompatActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void goToRegister(View view)
    {
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
    }

    public void goToMainMenu(View view)
    {
        Intent intent = new Intent(Login.this, MainMenu.class);
        startActivity(intent);
    }
}
