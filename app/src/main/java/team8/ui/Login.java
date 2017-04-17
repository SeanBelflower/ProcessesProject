package team8.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
        TextView usernameView = (TextView)findViewById(R.id.username);
        String username = usernameView.getText().toString();

        if(!username.isEmpty())
        {
            Intent intent = new Intent(Login.this, MainMenu.class);
            intent.putExtra("username", username);
            startActivity(intent);
        }
        else
        {
            TextView warning = (TextView)findViewById(R.id.warning);
            warning.setText("Please enter your username.");
        }
    }
}
